package org.example;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.ProtocolParamsSupplier;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.KupmiosBackendService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultProtocolParamsSupplier;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.function.Output;
import com.bloxbean.cardano.client.function.TxBuilder;
import com.bloxbean.cardano.client.function.TxBuilderContext;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;

import static com.bloxbean.cardano.client.common.ADAConversionUtil.adaToLovelace;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import static com.bloxbean.cardano.client.function.helper.BalanceTxBuilders.balanceTx;
import static com.bloxbean.cardano.client.function.helper.InputBuilders.createFromSender;
import static com.bloxbean.cardano.client.function.helper.SignerProviders.signerFrom;

@Slf4j
public class TransactionTest {
        
    private static String senderMnemonic = "flush together outer effort tenant photo waste distance rib grocery aunt broken weather arrow jungle debris finger flee casino doctor group echo baby near";
    private static Account senderAccount = new Account(Networks.testnet(), senderMnemonic);
    private static String receiverMnemonic = "essence pilot click armor alpha noise mixture soldier able advice multiply inject ticket pride airport uncover honey desert curtain sun true toast valve half";
    private static Account receiverAccount = new Account(Networks.testnet(), receiverMnemonic);
    private static UtxoSupplier utxoSupplier;
    private static ProtocolParamsSupplier protocolParamSupplier;
    private static TransactionService transactionService;
    private static TestHelper testHelper;
    private static BigInteger receiverBalanceBefore;
    private static BigInteger adaAmount = adaToLovelace(2.1);

    @BeforeAll
    static void setup() {

        BackendService backendService = new KupmiosBackendService("http://localhost:1337", "http://localhost:1442");
        utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        protocolParamSupplier = new DefaultProtocolParamsSupplier(backendService.getEpochService());
        transactionService = backendService.getTransactionService();
        testHelper = new TestHelper(backendService);
    }

    @Test
    void transfer_lovelace() throws Exception {
        String senderAddress = senderAccount.baseAddress();
        log.info("Sender address : " + senderAddress);
        System.out.println("sender balance before: " + testHelper.lovelaceBalance(senderAddress));

        String receiverAddress = receiverAccount.baseAddress();
        log.info("Receiver address : " + receiverAddress);
        receiverBalanceBefore = testHelper.lovelaceBalance(receiverAddress);
        System.out.println("receiver balance before: " + receiverBalanceBefore);

        Output output = Output.builder()
                .address(receiverAddress)
                .assetName(LOVELACE)
                .qty(adaAmount)
                .build();

        TxBuilder txBuilder = output.outputBuilder()
                .buildInputs(createFromSender(senderAccount.baseAddress(), senderAddress))
                .andThen(balanceTx(senderAddress, 1));

        Transaction signedTransaction = TxBuilderContext.init(utxoSupplier, protocolParamSupplier)
                .buildAndSign(txBuilder, signerFrom(senderAccount));

        System.out.println("Signed transaction: " + JsonUtil.getPrettyJson(signedTransaction));
        Result<String> result = transactionService.submitTransaction(signedTransaction.serialize());
        Thread.sleep(2000);
        System.out.println("Transaction result: " + JsonUtil.getPrettyJson(result));
        System.out.println("receiver after: " + testHelper.lovelaceBalance(receiverAddress));

        // Add assertion to verify the transaction
        BigInteger receiverBalanceAfter = testHelper.lovelaceBalance(receiverAddress);
        assert receiverBalanceAfter.equals(receiverBalanceBefore.add(adaAmount)) : 
            "Expected receiver balance to be " + receiverBalanceBefore.add(adaAmount) + " ADA, but was: " + receiverBalanceAfter;

    }
}