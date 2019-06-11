package com.runhang.shadow.server.core.databaseSync;

import com.runhang.shadow.server.core.model.DatabaseField;

public interface ShadowObserver {

    void onFieldUpdate(DatabaseField data);

}
