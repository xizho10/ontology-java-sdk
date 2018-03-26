
##Smart Contract 

* What would be the AbiInfo structure?

```
public class AbiInfo {
    public String hash;
    public String entrypoint;
    public List<AbiFunction> functions;
    public List<AbiEvent> events;
}
public class AbiFunction {
    public String name;
    public String returntype;
    public List<Parameter> parameters;
}
public class Parameter {
    public String name;
    public String type;
    public String value;
}
```

* What is codehash?

codehash is generated by calculating sha160 of the smart contract's bytecode twice. It is also the unique identifier of smart contract.

* What would sdk do in detail when calling the invokeTrasaction function of smart contract?

```
//step1：construct transaction
//Firstly, convert the parameters of smart contract into the vm-recognizable opcode 
Transaction tx = sdk.getSmartcodeTx().makeInvokeCodeTransaction(opcodes,codeHash,info.address,info.pubkey);

//step2：sign the transaction
String txHex = sdk.getWalletMgr().signatureData(password,tx);

//step3：send the transaction
sdk.getConnectMgr().sendRawTransaction(txHex);
```

* Why do we need to pass the account and its password whehn invoking?

User's signature, which is generated by the private key, is neccesary in the process of invoking a smart contract. And the private key is encrypted and stored in the wallet, need the password to decrypt.

* What is the pre-execution of smart contract when querying the assert and how to use it?

Operations of smart contract, such as get, do not need to go through any consensus node. They read data directly from the storage of smart contract, execute at current node, and return the result. We can call the pre-execution interface while sending transactions.

```
String result = (String) sdk.getConnectMgr().sendRawTransactionPreExec(txHex);
```

## Deployment of smart contract

#### **An deployment example of smart contract**

```
ontSdk.setCodeHash(Helper.getCodeHash(code));

//Deploy the contract
String txhash = ontSdk.getSmartcodeTx().DeployCodeTransaction(code, true, "name", "1.0", "author", "email", "desp", ContractParameterType.Boolean.name());
System.out.println("txhash:" + txhash);
//Waiting for block generation
Thread.sleep(6000);
DeployCodeTransaction t = (DeployCodeTransaction) ontSdk.getConnectMgr().getRawTransaction(txhash);
```
| Parameters    | Field       | Type                  | Description                       | Explaination                           |
| -----         | -------     | ------                | -------------                     | -----------                            |
| Input params  | codeHexStr  | String                | Contract code                     | Required                               |
|               | needStorage | String                | Need storage or not               | Required                               |
|               | name        | String                | Contract name                     | Required                               |
|               | codeVersion | String                | Contract version                  | Required                               |
|               | author      | String                | Contract author                   | Required                               |
|               | email       | String                | Author email                      | Required                               |
|               | desp        | String                | Description                       | Required                               |
|               | returnType  | ContractParameterType | Type of data returned by contract | Required                               |
| Output params | txid        | String                | Transaction ID                    | Transaction ID should be 64-bit string |

----

## invocation of smart contract

Read the abi file of smart contract, construct the function that calls the smart contract, and send transactions.
```
//Load the abi file of smart contract
InputStream is = new FileInputStream("C:\\ZX\\NeoContract1.abi.json");
byte[] bys = new byte[is.available()];
is.read(bys);
is.close();
String abi = new String(bys);
            
//Interpret the abi file
AbiInfo abiinfo = JSON.parseObject(abi, AbiInfo.class);
System.out.println("codeHash:"+abiinfo.getHash());
System.out.println("Entrypoint:"+abiinfo.getEntrypoint());
System.out.println("Functions:"+abiinfo.getFunctions());
System.out.println("Events"+abiinfo.getEvents());

//Set the codehash of smart contract
ontSdk.setCodeHash(abiinfo.getHash());

//Obtain the accound informations
Identity did = ontSdk.getWalletMgr().getIdentitys().get(0);
AccountInfo info = ontSdk.getWalletMgr().getAccountInfo(did.ontid,"passwordtest");

//Construct the smart contract function
AbiFunction func = abiinfo.getFunction("AddAttribute");
System.out.println(func.getParameters());
func.setParamsValue(did.ontid.getBytes(),"key".getBytes(),"bytes".getBytes(),"values02".getBytes(),Helper.hexToBytes(info.pubkey));
System.out.println(func);

//Call the smart contract
String hash = ontSdk.getSmartcodeTx().invokeTransaction(did.ontid,"passwordtest",func);

```

> Read the following chapter if you need to monitor the push notification

## Process the push notification of Samrt contract

Create websocket thread and analyse the push notification

Demo example：
```
String wsUrl = "ws://101.132.193.149:21335";

OntSdk ontSdk = getOntSdk();
Object lock = new Object();
WsProcess.startWebsocketThread(lock,wsUrl);
WsProcess.setBroadcast(true);
waitResult(ontSdk,lock);

public static void waitResult(OntSdk ontSdk, Object lock){
        try {
            synchronized (lock) {
                boolean flag = false;
                while(true) {
                    //Wait for new push
                    lock.wait();
                    //Heartbeat
                    if(MsgQueue.getChangeFlag()){
                        System.out.println(MsgQueue.getHeartBeat());
                    }
                    //acquire the push notification
                    for (String e : MsgQueue.getResultSet()) {
                        System.out.println("####"+e);
                        Result rt = JSON.parseObject(e, Result.class);
                        //TODO
                        MsgQueue.removeResult(e);
                        if(rt.Action.equals("Notify")) {
                            flag = true;
                            List<Map<String,Object>> list = (List<Map<String,Object>>)((Map)rt.Result).get("State");
                            for(Map m:(List<Map<String,Object>>)(list.get(0).get("Value"))){
                                String value = (String)m.get("Value");
                                String val = new String(Helper.hexToBytes(value));
                                System.out.print(val+" ");
                            }
                            System.out.println();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
 }

```


