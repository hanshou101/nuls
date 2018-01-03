package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseEvent<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable {
    private NulsDigestData hash;
    private EventHeader header;
    private T eventBody;
    private NulsSignData sign;

    public BaseEvent(short moduleId, short eventType, byte[] extend) {
        this.header = new EventHeader(moduleId, eventType, extend);
    }

    @Override
    public int size() {
        int size = Utils.sizeOfSerialize(header);
        size += Utils.sizeOfSerialize(eventBody);
        size += Utils.sizeOfSerialize(sign);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(header);
        stream.writeNulsData(this.eventBody);
        stream.writeNulsData(sign);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = byteBuffer.readNulsData(new EventHeader());
        this.eventBody = parseEventBody(byteBuffer);
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        this.sign = byteBuffer.readSign();

    }

    protected abstract T parseEventBody(NulsByteBuffer byteBuffer) throws NulsException;

    public T getEventBody() {
        return eventBody;
    }

    public void setEventBody(T eventBody) {
        this.eventBody = eventBody;
    }

    public EventHeader getHeader() {
        return header;
    }

    public void setHeader(EventHeader header) {
        this.header = header;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }

    public NulsDigestData getHash() {
        return hash;
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }
}
