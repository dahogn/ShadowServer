package com.runhang.shadow.client.core.shadow;

import com.runhang.shadow.client.core.model.DatabaseField;

public interface ShadowObserver {

    void onFieldUpdate(DatabaseField data);

}
