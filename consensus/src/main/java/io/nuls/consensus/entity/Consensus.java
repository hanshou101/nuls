/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import org.apache.tools.ant.taskdefs.Basename;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class Consensus<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable {

    private String address;

    private T extend;

    private NulsDigestData hash;

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfString(address);
        size += Utils.sizeOfNulsData(extend);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeNulsData(extend);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readString();
       this.extend = this.parseExtend(byteBuffer);
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
    }

    protected abstract T parseExtend(NulsByteBuffer byteBuffer) throws NulsException;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public T getExtend() {
        return extend;
    }

    public void setExtend(T extend) {
        this.extend = extend;
    }

    public NulsDigestData getHash() {
        if(null==hash){
            try {
                this. hash = NulsDigestData.calcDigestData(this.serialize());
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return hash;
    }

    public String getHexHash(){
        return this.getHash().getDigestHex();
    }

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    @Override
    public Object copy() {
        return this;
    }
}
