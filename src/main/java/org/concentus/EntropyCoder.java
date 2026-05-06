/* Copyright (c) 2001-2011 Timothy B. Terriberry
   Ported to Java by Logan Stromberg

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:

   - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   - Neither the name of Internet Society, IETF or IETF Trust, nor the
   names of specific contributors, may be used to endorse or promote
   products derived from this software without specific prior written
   permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
   OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.concentus;

/*A range decoder.
This is an entropy decoder based upon \cite{Mar79}, which is itself a
rediscovery of the FIFO arithmetic code introduced by \cite{Pas76}.
It is very similar to arithmetic encoding, except that encoding is done with
digits in any base, instead of with bits, and so it is faster when using
larger bases (i.e.: a byte).
The author claims an average waste of $\frac{1}{2}\log_b(2b)$ bits, where $b$
is the base, longer than the theoretical optimum, but to my knowledge there
is no published justification for this claim.
This only seems true when using near-infinite precision arithmetic so that
the process is carried out with no rounding errors.

An excellent description of implementation details is available at
http://www.arturocampos.com/ac_range.html
A recent work \cite{MNW98} which proposes several changes to arithmetic
encoding for efficiency actually re-discovers many of the principles
behind range encoding, and presents a good theoretical analysis of them.

End of stream is handled by writing out the smallest number of bits that
ensures that the stream will be correctly decoded regardless of the value of
any subsequent bits.
ec_tell() can be used to determine how many bits were needed to decode
all the symbols thus far; other data can be packed in the remaining bits of
the input buffer.
@PHDTHESIS{Pas76,
author="Richard Clark Pasco",
title="Source coding algorithms for fast data compression",
school="Dept. of Electrical Engineering, Stanford University",
address="Stanford, CA",
month=May,
year=1976
}
@INPROCEEDINGS{Mar79,
author="Martin, G.N.N.",
title="Range encoding: an algorithm for removing redundancy from a digitised
message",
booktitle="Video & Data Recording Conference",
year=1979,
address="Southampton",
month=Jul
}
@ARTICLE{MNW98,
author="Alistair Moffat and Radford Neal and Ian H. Witten",
title="Arithmetic Coding Revisited",
journal="{ACM} Transactions on Information Systems",
year=1998,
volume=16,
number=3,
pages="256--294",
month=Jul,
URL="http://www.stanford.edu/class/ee398a/handouts/papers/Moffat98ArithmCoding.pdf"
}*/
class EntropyCoder {

    private final int EC_WINDOW_SIZE = 32;

    ///*The number of bits to use for the range-coded part of uint integers.*/
    private final int EC_UINT_BITS = 8;

    ///*The resolution of fractional-precision bit usage measurements, i.e.,
    //   3 => 1/8th bits.*/
    static final int BITRES = 3;

    /*The number of bits to output at a time.*/
    private final int EC_SYM_BITS = 8;

    /*The total number of bits in each of the state registers.*/
    private final int EC_CODE_BITS = 32;

    /*The maximum symbol value.*/
    private final long EC_SYM_MAX = 0x000000FF;

    /*Bits to shift by to move a symbol into the high-order position.*/
    private final int EC_CODE_SHIFT = 0x00000017;

    /*Carry bit of the high-order range symbol.*/
    private final long EC_CODE_TOP = 0x80000000L;

    /*Low-order bit of the high-order range symbol.*/
    private final long EC_CODE_BOT = 0x00800000;

    /*The number of bits available for the last, partial symbol in the code field.*/
    private final int EC_CODE_EXTRA = 0x00000007;

    //////////////// Coder State //////////////////// 

    /*POINTER to Buffered input/output.*/
    private byte[] buf;
    private int buf_ptr;

    /*The size of the buffer.*/
    int storage;

    /*The offset at which the last byte containing raw bits was read/written.*/
    int end_offs;

    /*Bits that will be read from/written at the end.*/
    long end_window;

    /*Number of valid bits in end_window.*/
    int nend_bits;

    /*The total number of whole bits read/written.
      This does not include partial bits currently in the range coder.*/
    int nbits_total;

    /*The offset at which the next range coder byte will be read/written.*/
    int offs;

    /*The number of values in the current range.*/
    long rng;

    /*In the decoder: the difference between the top of the current range and
       the input value, minus one.
      In the encoder: the low end of the current range.*/
    long val;

    /*In the decoder: the saved normalization factor from ec_decode().
      In the encoder: the number of oustanding carry propagating symbols.*/
    long ext;

    /*A buffered input/output symbol, awaiting carry propagation.*/
    int rem;

    /*Nonzero if an error occurred.*/
    int error;

    EntropyCoder() {
        Reset();
    }

    void Reset() {
        buf = null;
        buf_ptr = 0;
        storage = 0;
        end_offs = 0;
        end_window = 0;
        nend_bits = 0;
        offs = 0;
        rng = 0;
        val = 0;
        ext = 0;
        rem = 0;
        error = 0;
    }

    void Assign(EntropyCoder other) {
        this.buf = other.buf;
        this.buf_ptr = other.buf_ptr;
        this.storage = other.storage;
        this.end_offs = other.end_offs;
        this.end_window = other.end_window;
        this.nend_bits = other.nend_bits;
        this.nbits_total = other.nbits_total;
        this.offs = other.offs;
        this.rng = other.rng;
        this.val = other.val;
        this.ext = other.ext;
        this.rem = other.rem;
        this.error = other.error;
    }

    byte[] get_buffer() {
        byte[] convertedBuf = new byte[this.storage];
        System.arraycopy(this.buf, this.buf_ptr, convertedBuf, 0, this.storage);
        return convertedBuf;
    }

    void write_buffer(byte[] data, int data_ptr, int target_offset, int size) {
        System.arraycopy(data, data_ptr, this.buf, this.buf_ptr + target_offset, size);
    }

    int read_byte() {
        return this.offs < this.storage ? Inlines.SignedByteToUnsignedInt(this.buf[buf_ptr + this.offs++]) : 0;
    }

    int read_byte_from_end() {
        return this.end_offs < this.storage
                ? Inlines.SignedByteToUnsignedInt(this.buf[buf_ptr + (this.storage - ++(this.end_offs))]) : 0;
    }

    int write_byte(long _value) {
        if (this.offs + this.end_offs >= this.storage) {
            return -1;
        }
        this.buf[buf_ptr + this.offs++] = (byte) (_value & 0xFF);
        return 0;
    }

    int write_byte_at_end(long _value) {
        if (this.offs + this.end_offs >= this.storage) {
            return -1;
        }

        this.buf[buf_ptr + (this.storage - ++(this.end_offs))] = (byte) (_value & 0xFF);
        return 0;
    }

    /// <summary>
    /// Normalizes the contents of val and rng so that rng lies entirely in the high-order symbol.
    /// </summary>
    /// <param name="this"></param>
    void dec_normalize() {
        /*If the range is too small, rescale it and input some bits.*/
        while (this.rng <= EC_CODE_BOT) {
            int sym;
            this.nbits_total += EC_SYM_BITS;
            this.rng = Inlines.CapToUInt32(this.rng << EC_SYM_BITS);

            /*Use up the remaining bits from our last symbol.*/
            sym = this.rem;

            /*Read the next value from the input.*/
            this.rem = read_byte();

            /*Take the rest of the bits we need from this new symbol.*/
            sym = (sym << EC_SYM_BITS | this.rem) >> (EC_SYM_BITS - EC_CODE_EXTRA);

            /*And subtract them from val, capped to be less than EC_CODE_TOP.*/
            this.val = Inlines.CapToUInt32((((long) this.val << EC_SYM_BITS) + (EC_SYM_MAX & ~sym))
                    & (EC_CODE_TOP - 1));
        }
    }

    void dec_init(byte[] _buf, int _buf_ptr, int _storage) {
        this.buf = _buf;
        this.buf_ptr = _buf_ptr;
        this.storage = _storage;
        this.end_offs = 0;
        this.end_window = 0;
        this.nend_bits = 0;
        /*This is the offset from which ec_tell() will subtract partial bits.
          The final value after the ec_dec_normalize() call will be the same as in
           the encoder, but we have to compensate for the bits that are added there.*/
        this.nbits_total = EC_CODE_BITS + 1
                - ((EC_CODE_BITS - EC_CODE_EXTRA) / EC_SYM_BITS) * EC_SYM_BITS;
        this.offs = 0;
        this.rng = 1 << EC_CODE_EXTRA;
        this.rem = read_byte();
        this.val = Inlines.CapToUInt32(this.rng - 1 - (this.rem >> (EC_SYM_BITS - EC_CODE_EXTRA)));
        this.error = 0;
        /*Normalize the interval.*/
        dec_normalize();
    }

    long decode(long _ft) {
        _ft = Inlines.CapToUInt32(_ft);
        this.ext = Inlines.CapToUInt32(this.rng / _ft);
        long s = Inlines.CapToUInt32(this.val / this.ext);
        return Inlines.CapToUInt32(_ft - Inlines.EC_MINI(Inlines.CapToUInt32(s + 1), _ft));
    }

    long decode_bin(int _bits) {
        this.ext = this.rng >> _bits;
        long s = Inlines.CapToUInt32(this.val / this.ext);
        return Inlines.CapToUInt32(Inlines.CapToUInt32(1L << _bits) - Inlines.EC_MINI(Inlines.CapToUInt32(s + 1), 1L << _bits));
    }

    void dec_update(long _fl, long _fh, long _ft) {
        _fl = Inlines.CapToUInt32(_fl);
        _fh = Inlines.CapToUInt32(_fh);
        _ft = Inlines.CapToUInt32(_ft);
        long s = Inlines.CapToUInt32(this.ext * (_ft - _fh));
        this.val = this.val - s;
        this.rng = _fl > 0 ? Inlines.CapToUInt32(this.ext * (_fh - _fl)) : this.rng - s;
        dec_normalize();
    }

    /// <summary>
    /// The probability of having a "one" is 1/(1<<_logp).
    /// </summary>
    /// <param name="this"></param>
    /// <param name="_logp"></param>
    /// <returns></returns>
    int dec_bit_logp(long _logp) {
        long r;
        long d;
        long s;
        int ret;
        r = this.rng;
        d = this.val;
        s = r >> (int) _logp;
        ret = d < s ? 1 : 0;
        if (ret == 0) {
            this.val = Inlines.CapToUInt32(d - s);
        }
        this.rng = ret != 0 ? s : r - s;
        dec_normalize();
        return ret;
    }

    int dec_icdf(short[] _icdf, int _ftb) {
        long t;
        long s = this.rng;
        long d = this.val;
        long r = s >> _ftb;
        int ret = -1;
        do {
            t = s;
            s = Inlines.CapToUInt32(r * _icdf[++ret]);
        } while (d < s);
        this.val = Inlines.CapToUInt32(d - s);
        this.rng = Inlines.CapToUInt32(t - s);
        dec_normalize();
        return ret;
    }

    int dec_icdf(short[] _icdf, int _icdf_offset, int _ftb) {
        long t;
        long s = this.rng;
        long d = this.val;
        long r = s >> _ftb;
        int ret = _icdf_offset - 1;
        do {
            t = s;
            s = Inlines.CapToUInt32(r * _icdf[++ret]);
        } while (d < s);
        this.val = Inlines.CapToUInt32(d - s);
        this.rng = Inlines.CapToUInt32(t - s);
        dec_normalize();
        return ret - _icdf_offset;
    }

    long dec_uint(long _ft) {
        _ft = Inlines.CapToUInt32(_ft);
        long ft;
        long s;
        int ftb;
        /*In order to optimize EC_ILOG(), it is undefined for the value 0.*/
        Inlines.OpusAssert(_ft > 1);
        _ft--;
        ftb = Inlines.EC_ILOG(_ft);
        if (ftb > EC_UINT_BITS) {
            long t;
            ftb -= EC_UINT_BITS;
            ft = Inlines.CapToUInt32((_ft >> ftb) + 1);
            s = Inlines.CapToUInt32(decode(ft));
            dec_update(s, (s + 1), ft);
            t = Inlines.CapToUInt32((s << ftb | dec_bits(ftb)));
            if (t <= _ft) {
                return t;
            }
            this.error = 1;
            return _ft;
        } else {
            _ft++;
            s = Inlines.CapToUInt32(decode(_ft));
            dec_update(s, s + 1, _ft);
            return s;
        }
    }

    int dec_bits(int _bits) {
        long window;
        int available;
        int ret;
        window = this.end_window;
        available = this.nend_bits;
        if (available < _bits) {
            do {
                window = Inlines.CapToUInt32(window | (read_byte_from_end() << available));
                available += EC_SYM_BITS;
            } while (available <= EC_WINDOW_SIZE - EC_SYM_BITS);
        }
        ret = (int) (0xFFFFFFFF & (window & ((1 << _bits) - 1)));
        window = window >> _bits;
        available = available - _bits;
        this.end_window = Inlines.CapToUInt32(window);
        this.nend_bits = available;
        this.nbits_total = this.nbits_total + _bits;
        return ret;
    }

    /// <summary>
    /// Outputs a symbol, with a carry bit.
    /// If there is a potential to propagate a carry over several symbols, they are
    /// buffered until it can be determined whether or not an actual carry will
    /// occur.
    /// If the counter for the buffered symbols overflows, then the stream becomes
    /// undecodable.
    /// This gives a theoretical limit of a few billion symbols in a single packet on
    /// 32-bit systems.
    /// The alternative is to truncate the range in order to force a carry, but
    /// requires similar carry tracking in the decoder, needlessly slowing it down.
    /// </summary>
    /// <param name="this"></param>
    /// <param name="_c"></param>
    void enc_carry_out(int _c) {
        if (_c != EC_SYM_MAX) {
            /*No further carry propagation possible, flush buffer.*/
            int carry;
            carry = _c >> EC_SYM_BITS;

            /*Don't output a byte on the first write.
              This compare should be taken care of by branch-prediction thereafter.*/
            if (this.rem >= 0) {
                this.error |= write_byte(Inlines.CapToUInt32(this.rem + carry));
            }

            if (this.ext > 0) {
                long sym;
                sym = (EC_SYM_MAX + carry) & EC_SYM_MAX;
                do {
                    this.error |= write_byte(sym);
                } while (--(this.ext) > 0);
            }

            this.rem = (int) (_c & EC_SYM_MAX);
        } else {
            this.ext++;
        }
    }

    void enc_normalize() {
        /*If the range is too small, output some bits and rescale it.*/
        while (this.rng <= EC_CODE_BOT) {
            enc_carry_out((int) (this.val >> EC_CODE_SHIFT));
            /*Move the next-to-high-order symbol into the high-order position.*/
            this.val = Inlines.CapToUInt32((this.val << EC_SYM_BITS) & (EC_CODE_TOP - 1));
            this.rng = Inlines.CapToUInt32(this.rng << EC_SYM_BITS);
            this.nbits_total += EC_SYM_BITS;
        }
    }

    void enc_init(byte[] _buf, int buf_ptr, int _size) {
        this.buf = _buf;
        this.buf_ptr = buf_ptr;
        this.end_offs = 0;
        this.end_window = 0;
        this.nend_bits = 0;
        /*This is the offset from which ec_tell() will subtract partial bits.*/
        this.nbits_total = EC_CODE_BITS + 1;
        this.offs = 0;
        this.rng = Inlines.CapToUInt32(EC_CODE_TOP);
        this.rem = -1;
        this.val = 0;
        this.ext = 0;
        this.storage = _size;
        this.error = 0;
    }

    void encode(long _fl, long _fh, long _ft) {
        _fl = Inlines.CapToUInt32(_fl);
        _fh = Inlines.CapToUInt32(_fh);
        _ft = Inlines.CapToUInt32(_ft);
        long r = Inlines.CapToUInt32(this.rng / _ft);
        if (_fl > 0) {
            this.val += Inlines.CapToUInt32(this.rng - (r * (_ft - _fl)));
            this.rng = Inlines.CapToUInt32(r * (_fh - _fl));
        } else {
            this.rng = Inlines.CapToUInt32(this.rng - (r * (_ft - _fh)));
        }

        enc_normalize();
    }

    void encode_bin(long _fl, long _fh, int _bits) {
        _fl = Inlines.CapToUInt32(_fl);
        _fh = Inlines.CapToUInt32(_fh);
        long r = Inlines.CapToUInt32(this.rng >> (int) _bits);
        if (_fl > 0) {
            this.val = Inlines.CapToUInt32(this.val + Inlines.CapToUInt32(this.rng - (r * ((1 << (int) _bits) - _fl))));
            this.rng = Inlines.CapToUInt32(r * (_fh - _fl));
        } else {
            this.rng = Inlines.CapToUInt32(this.rng - (r * ((1 << (int) _bits) - _fh)));
        }

        enc_normalize();
    }

    /*The probability of having a "one" is 1/(1<<_logp).*/
    void enc_bit_logp(int _val, int _logp) {
        long r = this.rng;
        long l = this.val;
        long s = r >> _logp;
        r -= s;
        if (_val != 0) {
            this.val = Inlines.CapToUInt32(l + r);
        }

        this.rng = _val != 0 ? s : r;
        enc_normalize();
    }

    void enc_icdf(int _s, short[] _icdf, int _ftb) {
        long r = Inlines.CapToUInt32(this.rng >> _ftb);
        if (_s > 0) {
            this.val = this.val + Inlines.CapToUInt32(this.rng - Inlines.CapToUInt32(r * _icdf[_s - 1]));
            this.rng = (r * Inlines.CapToUInt32(_icdf[_s - 1] - _icdf[_s]));
        } else {
            this.rng = Inlines.CapToUInt32(this.rng - (r * _icdf[_s]));
        }
        enc_normalize();
    }

    void enc_icdf(int _s, short[] _icdf, int icdf_ptr, int _ftb) {
        long r = Inlines.CapToUInt32(this.rng >> _ftb);
        if (_s > 0) {
            this.val = this.val + Inlines.CapToUInt32(this.rng - Inlines.CapToUInt32(r * _icdf[icdf_ptr + _s - 1]));
            this.rng = Inlines.CapToUInt32(r * Inlines.CapToUInt32(_icdf[icdf_ptr + _s - 1] - _icdf[icdf_ptr + _s]));
        } else {
            this.rng = Inlines.CapToUInt32(this.rng - (r * _icdf[icdf_ptr + _s]));
        }
        enc_normalize();
    }

    void enc_uint(long _fl, long _ft) {
        _fl = Inlines.CapToUInt32(_fl);
        _ft = Inlines.CapToUInt32(_ft);

        long ft;
        long fl;
        int ftb;
        /*In order to optimize EC_ILOG(), it is undefined for the value 0.*/
        Inlines.OpusAssert(_ft > 1);
        _ft--;
        ftb = Inlines.EC_ILOG(_ft);
        if (ftb > EC_UINT_BITS) {
            ftb -= EC_UINT_BITS;
            ft = Inlines.CapToUInt32((_ft >> ftb) + 1);
            fl = Inlines.CapToUInt32(_fl >> ftb);
            encode(fl, fl + 1, ft);
            enc_bits(_fl & Inlines.CapToUInt32((1 << ftb) - 1), ftb);
        } else {
            encode(_fl, _fl + 1, _ft + 1);
        }
    }

    void enc_bits(long _fl, int _bits) {
        _fl = Inlines.CapToUInt32(_fl);
        long window;
        int used;
        window = this.end_window;
        used = this.nend_bits;
        Inlines.OpusAssert(_bits > 0);

        if (used + _bits > EC_WINDOW_SIZE) {
            do {
                this.error = this.error | write_byte_at_end(window & EC_SYM_MAX);
                window >>= EC_SYM_BITS;
                used -= EC_SYM_BITS;
            } while (used >= EC_SYM_BITS);
        }

        window = window | Inlines.CapToUInt32(_fl << used);
        used += _bits;
        this.end_window = window;
        this.nend_bits = used;
        this.nbits_total += _bits;
    }

    void enc_patch_initial_bits(long _val, int _nbits) {
        int shift;
        long mask;
        Inlines.OpusAssert(_nbits <= EC_SYM_BITS);
        shift = EC_SYM_BITS - _nbits;
        mask = ((1 << _nbits) - 1) << shift;

        if (this.offs > 0) {
            /*The first byte has been finalized.*/
            this.buf[buf_ptr] = (byte) ((this.buf[buf_ptr] & ~mask) | Inlines.CapToUInt32(_val << shift));
        } else if (this.rem >= 0) {
            /*The first byte is still awaiting carry propagation.*/
            this.rem = (int) Inlines.CapToUInt32((Inlines.CapToUInt32((this.rem & ~mask) | _val)) << shift);
        } else if (this.rng <= (EC_CODE_TOP >> (int) _nbits)) {
            /*The renormalization loop has never been run.*/
            this.val = Inlines.CapToUInt32((this.val & ~(mask << EC_CODE_SHIFT))
                    | Inlines.CapToUInt32(Inlines.CapToUInt32(_val) << (EC_CODE_SHIFT + shift)));
        } else {
            /*The encoder hasn't even encoded _nbits of data yet.*/
            this.error = -1;
        }
    }

    void enc_shrink(int _size) {
        Inlines.OpusAssert(this.offs + this.end_offs <= _size);
        Arrays.MemMove(this.buf, buf_ptr + (int) _size - (int) this.end_offs, buf_ptr + (int) this.storage - (int) this.end_offs, (int) this.end_offs);
        this.storage = _size;
    }

    int range_bytes() {
        return this.offs;
    }

    int get_error() {
        return this.error;
    }

    /// <summary>
    /// Returns the number of bits "used" by the encoded or decoded symbols so far.
    /// This same number can be computed in either the encoder or the decoder, and is
    /// suitable for making coding decisions.
    /// This will always be slightly larger than the exact value (e.g., all
    /// rounding error is in the positive direction).
    /// </summary>
    /// <param name="this"></param>
    /// <returns>The number of bits.</returns>
    int tell() {
        int returnVal = this.nbits_total - Inlines.EC_ILOG(this.rng);
        return returnVal;
    }

    private static final int[] correction = {35733, 38967, 42495, 46340, 50535, 55109, 60097, 65535};

    /// <summary>
    /// This is a faster version of ec_tell_frac() that takes advantage
    /// of the low(1/8 bit) resolution to use just a linear function
    /// followed by a lookup to determine the exact transition thresholds.
    /// </summary>
    /// <param name="this"></param>
    /// <returns></returns>
    int tell_frac() {
        int nbits;
        int r;
        int l;
        long b;
        nbits = this.nbits_total << BITRES;
        l = Inlines.EC_ILOG(this.rng);
        r = (int) (this.rng >> (l - 16));
        b = Inlines.CapToUInt32((r >> 12) - 8);
        b = Inlines.CapToUInt32(b + (r > correction[(int) b] ? 1 : 0));
        l = (int) ((l << 3) + b);
        return nbits - l;
    }

    void enc_done() {
        long window;
        int used;
        long msk;
        long end;
        int l;
        /*We output the minimum number of bits that ensures that the symbols encoded
           thus far will be decoded correctly regardless of the bits that follow.*/
        l = EC_CODE_BITS - Inlines.EC_ILOG(this.rng);
        msk = Inlines.CapToUInt32((EC_CODE_TOP - 1) >>> l);
        end = Inlines.CapToUInt32((Inlines.CapToUInt32(this.val + msk)) & ~msk);

        if ((end | msk) >= this.val + this.rng) {
            l++;
            msk >>= 1;
            end = Inlines.CapToUInt32((Inlines.CapToUInt32(this.val + msk)) & ~msk);
        }

        while (l > 0) {
            enc_carry_out((int) (end >> EC_CODE_SHIFT));
            end = Inlines.CapToUInt32((end << EC_SYM_BITS) & (EC_CODE_TOP - 1));
            l -= EC_SYM_BITS;
        }

        /*If we have a buffered byte flush it into the output buffer.*/
        if (this.rem >= 0 || this.ext > 0) {
            enc_carry_out(0);
        }

        /*If we have buffered extra bits, flush them as well.*/
        window = this.end_window;
        used = this.nend_bits;

        while (used >= EC_SYM_BITS) {
            this.error |= write_byte_at_end(window & EC_SYM_MAX);
            window = window >> EC_SYM_BITS;
            used -= EC_SYM_BITS;
        }

        /*Clear any excess space and add any remaining extra bits to the last byte.*/
        if (this.error == 0) {
            Arrays.MemSetWithOffset(this.buf, (byte) 0, buf_ptr + (int) this.offs, (int) this.storage - (int) this.offs - (int) this.end_offs);
            if (used > 0) {
                /*If there's no range coder data at all, give up.*/
                if (this.end_offs >= this.storage) {
                    this.error = -1;
                } else {
                    l = -l;
                    /*If we've busted, don't add too many extra bits to the last byte; it
                       would corrupt the range coder data, and that's more important.*/
                    if (this.offs + this.end_offs >= this.storage && l < used) {
                        window = Inlines.CapToUInt32(window & ((1 << l) - 1));
                        this.error = -1;
                    }

                    int z = buf_ptr + this.storage - this.end_offs - 1;
                    this.buf[z] = (byte) (this.buf[z] | (byte) (window & 0xFF));
                }
            }
        }
    }
}
