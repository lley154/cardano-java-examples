package org.example;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.ProtocolParamsSupplier;
import com.bloxbean.cardano.client.api.util.PolicyUtil;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.KupmiosBackendService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultProtocolParamsSupplier;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.common.model.Networks;
import static com.bloxbean.cardano.client.function.helper.BalanceTxBuilders.balanceTxWithAdditionalSigners;
import com.bloxbean.cardano.client.function.Output;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.function.helper.MintCreators;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.Policy;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.client.util.JsonUtil;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.List;

import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromSender;
import static com.bloxbean.cardano.client.function.helper.SignerProviders.signerFrom;

@Slf4j
public class MintTest {
        
    private static String senderMnemonic = "flush together outer effort tenant photo waste distance rib grocery aunt broken weather arrow jungle debris finger flee casino doctor group echo baby near";
    private static Account senderAccount = new Account(Networks.testnet(), senderMnemonic);
    private static String receiverMnemonic = "essence pilot click armor alpha noise mixture soldier able advice multiply inject ticket pride airport uncover honey desert curtain sun true toast valve half";
    private static Account receiverAccount = new Account(Networks.testnet(), receiverMnemonic);
    private static UtxoSupplier utxoSupplier;
    private static ProtocolParamsSupplier protocolParamSupplier;
    private static TransactionService transactionService;
    private static TestHelper testHelper;
    private static BigInteger receiverBalanceBeforeAsset1   ;
    private static BigInteger receiverBalanceBeforeAsset2;

    @BeforeAll
    static void setup() {

        BackendService backendService = new KupmiosBackendService("http://localhost:1337", "http://localhost:1442");
        utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        protocolParamSupplier = new DefaultProtocolParamsSupplier(backendService.getEpochService());
        transactionService = backendService.getTransactionService();
        testHelper = new TestHelper(backendService);
    }

    @Test
    void mint_transfer_asset() throws Exception {
    
        String senderAddress = senderAccount.baseAddress();
        log.info("Sender address : " + senderAddress);
        System.out.println("sender balance before: " + testHelper.lovelaceBalance(senderAddress));

        String receiverAddress = receiverAccount.baseAddress();
        log.info("Receiver address : " + receiverAddress);
        
        Policy policy = PolicyUtil.createMultiSigScriptAllPolicy("policy", 1);
        String assetName1 = "abc";
        Long assetQty1 = 1000L;
        String assetName2 = "xyz";
        Long assetQty2 = 2000L;

        String unit1 = policy.getPolicyId() + HexUtil.encodeHexString(assetName1.getBytes());
        String unit2 = policy.getPolicyId() + HexUtil.encodeHexString(assetName2.getBytes());

        receiverBalanceBeforeAsset1 = testHelper.assetBalance(receiverAddress, unit1);
        receiverBalanceBeforeAsset2 = testHelper.assetBalance(receiverAddress, unit2);
        System.out.println("receiver asset1 balance before: " + receiverBalanceBeforeAsset1);
        System.out.println("receiver asset2 balance before: " + receiverBalanceBeforeAsset2);

        MultiAsset multiAsset = MultiAsset
                .builder()
                .policyId(policy.getPolicyId())
                .assets(List.of(
                        new Asset(assetName1 , BigInteger.valueOf(assetQty1)),
                        new Asset(assetName2, BigInteger.valueOf(assetQty2))))
                .build();

        Output output1 = Output.builder()
                .address(receiverAddress)
                .policyId(policy.getPolicyId())
                .assetName(assetName1)
                .qty(BigInteger.valueOf(1000))
                .build();

        Output output2 = Output.builder()
                .address(receiverAddress)
                .policyId(policy.getPolicyId())
                .assetName(assetName2)
                .qty(BigInteger.valueOf(2000))
                .build();

        TxBuilder txBuilder = output1.mintOutputBuilder()
                .and(output2.mintOutputBuilder())
                .buildInputs(createFromSender(senderAddress, senderAddress))
                .andThen(MintCreators.mintCreator(policy.getPolicyScript(), multiAsset))
                .andThen(balanceTxWithAdditionalSigners(senderAddress, 1));

        Transaction signedTransaction = TxBuilderContext.init(utxoSupplier, protocolParamSupplier)
                .buildAndSign(txBuilder, signerFrom(senderAccount)
                .andThen(signerFrom(policy.getPolicyKeys().get(0))));

        System.out.println("Signed transaction: " + JsonUtil.getPrettyJson(signedTransaction));
        Result<String> result = transactionService.submitTransaction(signedTransaction.serialize());
        
        // wait for transaction to be included in the blockchain
        Thread.sleep(2000);
        System.out.println("Transaction result: " + JsonUtil.getPrettyJson(result));
        
        BigInteger receiverBalanceAfterAsset1 = testHelper.assetBalance(receiverAddress, unit1);
        BigInteger receiverBalanceAfterAsset2 = testHelper.assetBalance(receiverAddress, unit2);
        
        System.out.println("receiver asset1 balance after: " + receiverBalanceAfterAsset1);
        System.out.println("receiver asset2 balance after: " + receiverBalanceAfterAsset2);
        
        assert receiverBalanceAfterAsset1.equals(receiverBalanceBeforeAsset1.add(BigInteger.valueOf(assetQty1))) : 
            "Expected receiver asset1 balance to be " + receiverBalanceBeforeAsset1.add(BigInteger.valueOf(assetQty1)) + ", but was: " + receiverBalanceAfterAsset1;
        
        assert receiverBalanceAfterAsset2.equals(receiverBalanceBeforeAsset2.add(BigInteger.valueOf(assetQty2))) : 
            "Expected receiver asset2 balance to be " + receiverBalanceBeforeAsset2.add(BigInteger.valueOf(assetQty2)) + ", but was: " + receiverBalanceAfterAsset2;

    }
}