package com.island.nature.entities.components;

import com.island.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Component that defines an animal's sensory capabilities.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenseComponent implements Component {
    private int visionRadius;
    private int hearingRadius;
}
