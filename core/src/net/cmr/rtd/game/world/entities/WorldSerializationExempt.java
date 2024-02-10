package net.cmr.rtd.game.world.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.cmr.rtd.game.world.Entity;

/**
 * This annotation is used to exempt an {@link Entity} from {@link World} serialization
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WorldSerializationExempt {
    
}
