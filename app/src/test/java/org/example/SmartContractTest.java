package org.example;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.KupmiosBackendService;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.api.DefaultUtxoSupplier;
import com.bloxbean.cardano.client.common.model.Networks;
import static com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE;
import com.bloxbean.cardano.client.function.helper.ScriptUtxoFinders;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.blueprint.PlutusBlueprintUtil;
import com.bloxbean.cardano.client.plutus.blueprint.model.PlutusVersion;
import com.bloxbean.cardano.client.plutus.spec.BytesPlutusData;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.ScriptTx;
import com.bloxbean.cardano.client.quicktx.Tx;
import com.bloxbean.cardano.client.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.math.BigInteger;

/* Aiken Smart Contract

use aiken/collection/list
use aiken/crypto.{VerificationKeyHash}
use cardano/transaction.{OutputReference, Transaction}
 
pub type Datum {
  beneficiary: VerificationKeyHash,
}
 
pub type Redeemer {
  msg: ByteArray,
}
 
validator hello_world {
  spend(
    datum: Option<Datum>,
    redeemer: Redeemer,
    _own_ref: OutputReference,
    self: Transaction,
  ) {
    expect Some(Datum { beneficiary }) = datum
    let must_say_hello = redeemer.msg == "Hello, World!"
    let must_be_signed = list.has(self.extra_signatories, beneficiary)
    must_say_hello && must_be_signed
  }
}
*/

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)   
public class SmartContractTest {
        
    private static String senderMnemonic = "flush together outer effort tenant photo waste distance rib grocery aunt broken weather arrow jungle debris finger flee casino doctor group echo baby near";
    private static Account senderAccount = new Account(Networks.testnet(), senderMnemonic);
    private static String receiverMnemonic = "essence pilot click armor alpha noise mixture soldier able advice multiply inject ticket pride airport uncover honey desert curtain sun true toast valve half";
    private static Account receiverAccount = new Account(Networks.testnet(), receiverMnemonic);
    private static String compiledCode = "59019a01010032323232323232323225333003323232323253330083370e900118051baa001132323253333330120051533300b3370e900018069baa005132533301000100b132533333301400100c00c00c00c132533301230140031533300e3370e900018081baa004132533300f3371e6eb8c050c048dd5004a450d48656c6c6f2c20576f726c642100100114a06644646600200200644a66602c00229404c94ccc04ccdc79bae301800200414a226600600600260300026eb0c04cc050c050c050c050c050c050c050c050c044dd50051bae301330113754602660226ea801054cc03d24012a65787065637420536f6d6528446174756d207b2062656e6566696369617279207d29203d20646174756d001600d375c0026022002601c6ea8014028028028028028c03cc040008c038004c02cdd50008b1806180680118058009805801180480098031baa001149854cc0112411856616c696461746f722072657475726e65642066616c73650013656153300249011272656465656d65723a2052656465656d657200165734ae7155ceaab9e5573eae855d12ba41";
    private static PlutusScript plutusScript = PlutusBlueprintUtil.getPlutusScriptFromCompiledCode(compiledCode, PlutusVersion.v3);
    private static String scriptAddress = AddressProvider.getEntAddress(plutusScript, Networks.testnet()).toBech32();
    private static TestHelper testHelper;
    private static BigInteger scriptBalanceBefore;
    private static BigInteger senderBalanceBefore;
    private static BigInteger receiverBalanceBefore;
    private static Amount adaAmount = Amount.ada(10);
    private static BackendService backendService;

    @BeforeAll
    static void setup() {

        backendService = new KupmiosBackendService("http://localhost:1337", "http://localhost:1442");
        testHelper = new TestHelper(backendService);
    }

    @Test
    @Order(1)
    void lock_lovelace() throws Exception {

        System.out.println("================================================");
        System.out.println("Test lock_lovelace");
        String senderAddress = senderAccount.baseAddress();
        System.out.println("Sender address: " + senderAddress);
        senderBalanceBefore = testHelper.lovelaceBalance(senderAddress);
        System.out.println("sender balance before: " + senderBalanceBefore);

        System.out.println("Script address: " + scriptAddress);
        scriptBalanceBefore = testHelper.assetBalance(scriptAddress, LOVELACE);
        System.out.println("script balance before: " + scriptBalanceBefore);

        // Create datum
        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(receiverAccount.getBaseAddress().getPaymentCredentialHash().get()));

        // Build transaction
        Tx tx = new Tx()
                .payToContract(scriptAddress, adaAmount, datum)
                .from(senderAccount.baseAddress());
        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        
        // Sign and submit transaction
        Result<String> result = quickTxBuilder.compose(tx)
                .withSigner(SignerProviders.signerFrom(senderAccount))
                .completeAndWait(System.out::println);
        
        log.info("lock_lovelace result: " + JsonUtil.getPrettyJson(result));
        System.out.println("Sender address after: " + testHelper.assetBalance(senderAddress, LOVELACE));
        System.out.println("Script address after: " + testHelper.assetBalance(scriptAddress, LOVELACE));

        // Add assertion to verify the transaction
        BigInteger scriptBalanceAfter = testHelper.assetBalance(scriptAddress, LOVELACE);
        assert scriptBalanceAfter.equals(scriptBalanceBefore.add(adaAmount.getQuantity())) : 
            "Expected receiver balance to be " + scriptBalanceBefore.add(adaAmount.getQuantity()) + " ADA, but was: " + scriptBalanceAfter;

    }

    @Test
    @Order(2)
    void unlock_lovelace() throws Exception {

        System.out.println("================================================");
        System.out.println("Test unlock_lovelace");
        String recieverAddress = receiverAccount.baseAddress();
        System.out.println("Receiver address : " + recieverAddress);
        receiverBalanceBefore = testHelper.lovelaceBalance(recieverAddress);
        System.out.println("Receiver balance before: " + receiverBalanceBefore);

        System.out.println("Script address : " + scriptAddress);
        scriptBalanceBefore = testHelper.assetBalance(scriptAddress, LOVELACE);
        System.out.println("script balance before: " + scriptBalanceBefore);

        PlutusData datum = ConstrPlutusData.of(0, BytesPlutusData.of(receiverAccount.getBaseAddress().getPaymentCredentialHash().get()));

        UtxoSupplier utxoSupplier = new DefaultUtxoSupplier(backendService.getUtxoService());
        Utxo scriptUtxo = ScriptUtxoFinders.findFirstByInlineDatum(utxoSupplier, scriptAddress, datum).orElseThrow();

        // Create redeemer
        PlutusData redeemer = ConstrPlutusData.of(0, BytesPlutusData.of("Hello, World!"));

        // Build transaction
        ScriptTx scriptTx = new ScriptTx()
                .collectFrom(scriptUtxo, redeemer)
                .payToAddress(receiverAccount.baseAddress(), adaAmount)
                .attachSpendingValidator(plutusScript);
        QuickTxBuilder quickTxBuilder = new QuickTxBuilder(backendService);
        
        // Sign and submit transaction
        Result<String> result = quickTxBuilder.compose(scriptTx)
                .feePayer(receiverAccount.baseAddress())
                .collateralPayer(receiverAccount.baseAddress())
                .withSigner(SignerProviders.signerFrom(receiverAccount))
                .withRequiredSigners(receiverAccount.getBaseAddress())
                .completeAndWait(System.out::println);

        log.info("unlock_lovelace: " + JsonUtil.getPrettyJson(result));
        System.out.println("Receiver address after: " + testHelper.assetBalance(recieverAddress, LOVELACE));
        System.out.println("Script address after: " + testHelper.assetBalance(scriptAddress, LOVELACE));

        // Add assertion to verify the transaction
        BigInteger scriptBalanceAfter = testHelper.assetBalance(scriptAddress, LOVELACE);
        assert scriptBalanceAfter.equals(scriptBalanceBefore.subtract(adaAmount.getQuantity())) : 
            "Expected script balance to be " + scriptBalanceBefore.subtract(adaAmount.getQuantity()) + " ADA, but was: " + scriptBalanceAfter;

    }
}