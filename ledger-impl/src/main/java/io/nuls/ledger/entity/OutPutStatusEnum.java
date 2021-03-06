package io.nuls.ledger.entity;

public enum OutPutStatusEnum {

    UTXO_CONFIRM_UNSPEND,

    UTXO_UNCONFIRM_UNSPEND,

    UTXO_CONFIRM_TIME_LOCK,

    UTXO_UNCONFIRM_TIME_LOCK,

    UTXO_CONFIRM_CONSENSUS_LOCK,

    UTXO_UNCONFIRM_CONSENSUS_LOCK,

    UTXO_CONFIRM_SPEND,

    UTXO_UNCONFIRM_SPEND,

    UTXO_SPENT;
}
