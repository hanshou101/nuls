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

package io.nuls.client.processor;

import io.nuls.client.helper.CommandBulider;
import io.nuls.client.helper.CommandHelper;
import io.nuls.client.entity.CommandResult;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.resources.form.TransferForm;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.AccountService;
import io.nuls.rpc.sdk.service.WalletService;

import java.util.List;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/3/27
 */
public abstract class WalletProcessors implements CommandProcessor {

    protected WalletService walletService = WalletService.WALLET_SERVICE;
    protected AccountService accountService = AccountService.ACCOUNT_SERVICE;

    public static class SetPassword extends WalletProcessors {

        @Override
        public String getCommand() {
            return "setpwd";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<password> 密码 - 必输, 长度8-20位, 必须同时包含大小写字母和数字");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "setpwd <password> --set password of the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
            String newPwd = args[1];
            CommandHelper.confirmPwd(newPwd);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = walletService.setPassword(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


    public static class ResetPassword extends WalletProcessors {

        @Override
        public String getCommand() {
            return "resetpwd";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<oldpassword> 现有密码 - 必输")
                    .newLine("\t<newpassword> 新密码 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "resetpwd <oldpassword> <newpassword> --reset password for your wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
            String newPwd = args[2];
            CommandHelper.confirmPwd(newPwd);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = walletService.resetPassword(args[1], args[2]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    /**
     * nuls transfer
     */
    public static class Transfer extends WalletProcessors {

        private ThreadLocal<TransferForm> paramsData = new ThreadLocal<>();

        @Override
        public String getCommand() {
            return "transfer";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> 转账账户地址 - 必输")
                    .newLine("\t<toaddress> 接收人地址 - 必输")
                    .newLine("\t<amount> 转账金额 - 必输")
                    .newLine("\t<password> 钱包密码 - 必输")
                    .newLine("\t[remark] 备注 - 选填");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "transfer <address> <toAddress> <amount> <password> [remark] --transfer";
        }

        @Override
        public boolean argsValidate(String[] args) {
            boolean result;
            do {
                int length = args.length;
                if (length != 5 && length != 6) {
                    result = false;
                    break;
                }
                if(!CommandHelper.checkArgsIsNull(args)) {
                    result = false;
                    break;
                }
                TransferForm form = getTransferForm(args);
                paramsData.set(form);
                result = StringUtils.isNotBlank(form.getToAddress());
                if (!result) {
                    break;
                }
                result = form.getAmount() != null && form.getAmount() > 0;
            } while (false);
            return result;
        }

        private TransferForm getTransferForm(String[] args) {
            TransferForm form = null;
            Long amount = null;
            try {
                amount = CommandHelper.getLongAmount(args[3]);
                if(amount != null) {
                    form = new TransferForm();
                } else {
                    return null;
                }

            } catch (Exception e) {
                return null;
            }
            switch (args.length) {
                case 5:
                    form.setAddress(args[1]);
                    form.setToAddress(args[2]);
                    form.setAmount(amount);
                    form.setPassword(args[4]);
                    break;
                case 6:
                    form.setAddress(args[1]);
                    form.setToAddress(args[2]);
                    form.setAmount(amount);
                    form.setPassword(args[4]);
                    form.setRemark(args[5]);
                    break;
            }
            return form;
        }


        @Override
        public CommandResult execute(String[] args) {
            TransferForm form = paramsData.get();
            if (null == form) {
                form = getTransferForm(args);
            }
            RpcClientResult result = this.walletService.transfer(form.getAddress(), form.getToAddress(), form.getAmount(), form.getPassword(), form.getRemark());
            return CommandResult.getResult(result);
        }
    }

    public static class BackupWallet extends WalletProcessors {

        @Override
        public String getCommand() {
            return "backup";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<password> 密码 - 必输")
                    .newLine("\t[address] 备份账户地址，默认不传视为备份钱包所有账户 - 选填");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "backup <password> [address] --backup the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length < 2)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            int length = args.length;
            String address = null;
            if(length == 3) {
                address = args[2];
            }
            RpcClientResult result = walletService.backup(args[1], address);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    /**
     * get the balance of a address
     */
    public static class ImportAccount extends WalletProcessors {

        @Override
        public String getCommand() {
            return "import";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<prikey> 明文私钥 - 必输")
                    .newLine("\t<newpassword> 钱包密码 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "import <prikey> <password> --import an account by prikey";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
            String pwd = args[2];
            CommandHelper.checkAndConfirmPwd(pwd);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = walletService.importAccount(args[1],args[2]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class RemoveAccount extends WalletProcessors {

        @Override
        public String getCommand() {
            return "remove";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> 账户地址 - 必输")
                    .newLine("\t<password> 钱包密码 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "remove <address> <password> --remove an account from wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = walletService.removeAccount(args[1],args[2]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


}
