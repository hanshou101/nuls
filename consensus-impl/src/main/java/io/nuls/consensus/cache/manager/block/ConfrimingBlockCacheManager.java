/*
 *
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
 *
 */
package io.nuls.consensus.cache.manager.block;

import io.nuls.cache.util.CacheMap;
import io.nuls.consensus.constant.ConsensusCacheConstant;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.List;

/**
 * cache block data
 * @author Niels
 * @date 2017/12/12
 */
public class ConfrimingBlockCacheManager {
    private static final ConfrimingBlockCacheManager INSTANCE = new ConfrimingBlockCacheManager();

    private CacheMap<String, BlockHeader> headerCacheMap;
    private CacheMap<String, List<Transaction>> txsCacheMap;

    private ConfrimingBlockCacheManager() {
    }

    public static ConfrimingBlockCacheManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        txsCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_TXS_CACHE_NAME, 256, 0, 0);
        headerCacheMap = new CacheMap<>(ConsensusCacheConstant.BLOCK_HEADER_CACHE_NAME, 64, 0, 0);
    }

    public BlockHeader getBlockHeader(String hash) {
        if (headerCacheMap == null) {
            return null;
        }
        return headerCacheMap.get(hash);
    }

    public boolean cacheBlock(Block block) {
        if (null == block || null == block.getHeader() || block.getTxs().isEmpty()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, "The block is wrong!");
        }
        if(headerCacheMap.isEmpty()&&!block.getHeader().getPreHash().getDigestHex().equals(NulsContext.getInstance().getBestBlock().getHeader().getHash().getDigestHex())){
            return false;
        }
        if (!headerCacheMap.isEmpty()&&!headerCacheMap.containsKey(block.getHeader().getPreHash().getDigestHex())) {
            return false;
        }
        String hash = block.getHeader().getHash().getDigestHex();
        headerCacheMap.put(hash, block.getHeader());
        txsCacheMap.put(hash, block.getTxs());
        return true;
    }


    public Block getBlock(String hash) {
        if (null == txsCacheMap || headerCacheMap == null) {
            return null;
        }
        BlockHeader header = headerCacheMap.get(hash);
        List<Transaction> txs = txsCacheMap.get(hash);
        if (null == header || null == txs || txs.isEmpty()) {
            return null;
        }
        Block block = new Block();
        block.setHeader(header);
        block.setTxs(txs);
        return block;
    }

    public void clear() {
        this.txsCacheMap.clear();
        this.headerCacheMap.clear();
    }

    public void destroy() {
        this.txsCacheMap.destroy();
        this.headerCacheMap.destroy();
    }

    public void removeBlock(String hash) {
        this.txsCacheMap.remove(hash);
        this.headerCacheMap.remove(hash);
    }

    public CacheMap<String, BlockHeader> getHeaderCacheMap() {
        return headerCacheMap;
    }

    public CacheMap<String, List<Transaction>> getTxsCacheMap() {
        return txsCacheMap;
    }
}
