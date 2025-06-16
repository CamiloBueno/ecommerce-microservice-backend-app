package com.selimhorri.app.config.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum OrderFeatures implements Feature {
    
    @Label("Descount Feature for Orders")
    DISCOUNT_ON_ORDER_CREATION;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

}
