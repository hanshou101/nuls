package io.nuls.ledger.module;

import io.nuls.core.module.NulsModule;
import io.nuls.ledger.event.BaseLedgerEvent;
import io.nuls.ledger.event.CoinTransactionEvent;

/**
 * Created by Niels on 2017/11/7.
 */
public abstract class LedgerModule extends NulsModule {
    public LedgerModule() {
        super("ledger");
        this.registerEvent((short) 1, BaseLedgerEvent.class);
        this.registerEvent((short) 2, CoinTransactionEvent.class);
    }
}