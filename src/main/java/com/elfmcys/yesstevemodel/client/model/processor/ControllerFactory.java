package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;

import java.util.function.Consumer;

public interface ControllerFactory<T extends GeoEntity<?>> {
    void create(T entity, Consumer<IAnimationController<T>> consumer);
}