package com.westreicher.birdsim.artemis.components;

import com.artemis.Component;
import com.westreicher.birdsim.artemis.managers.ModelManager;

/**
 * Created by david on 9/28/15.
 */
public class ModelComponent extends Component {

    public boolean visible;
    public float scale;


    public ModelManager.Models type;

    public ModelComponent(ModelManager.Models m) {
        this.type = m;
        this.scale = 5f;
    }

}
