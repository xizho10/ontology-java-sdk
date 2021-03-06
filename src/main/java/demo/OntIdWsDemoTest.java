/*
 * Copyright (C) 2018 The ontology Authors
 * This file is part of The ontology library.
 *
 *  The ontology is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  The ontology is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with The ontology.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package demo;

import com.github.ontio.common.Helper;
import com.github.ontio.core.payload.InvokeCode;
import com.github.ontio.OntSdk;
import com.github.ontio.sdk.wallet.Identity;
import com.github.ontio.sdk.wallet.Wallet;
import com.github.ontio.sdk.websocket.MsgQueue;
import com.github.ontio.sdk.websocket.Result;
import com.github.ontio.sdk.websocket.WsProcess;
import com.alibaba.fastjson.JSON;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 */

public class OntIdWsDemoTest {
    public static void main(String[] args) {
        try {
            OntSdk ontSdk = getOntSdk();
            Identity accountInfo = ontSdk.getWalletMgr().importIdentity("6PYMpk8DjWzaEvneyaqxMBap9DuUPH72W6BsWWTtpWE4JJZkGq5ENtfYbT", "passwordtest");
            // System.out.println(accountInfo);
            //  System.exit(0);
//            System.out.println(ontSdk.getWalletMgr().getWallet().getIdentities());
            ontSdk.getWalletMgr().getWallet().setDefaultIdentity(0);
            ontSdk.getWalletMgr().writeWallet();

            String wsUrl = "ws://127.0.0.1:20385";
//            String wsUrl = "ws://54.222.182.88:22335";
//            String wsUrl = "ws://101.132.193.149:21335";

            Object lock = new Object();
            WsProcess.startWebsocketThread(lock, wsUrl);

            //wait ws session uuid，asign websocket client
            String wsUUID = waitUserid(ontSdk, lock);
            System.out.println("wsSessionID:" + wsUUID);
            ontSdk.setWsSessionId(wsUUID);

            Wallet oep6 = ontSdk.getWalletMgr().getWallet();
            System.out.println("oep6:" + JSON.toJSONString(oep6));
            //System.exit(0);

            System.out.println("================register=================");
            //registry ontid
            Identity ident = ontSdk.getOntIdTx().register("passwordtest");

            String ontid = ident.ontid;

            //System.exit(0);
            //
            waitResult(ontSdk, lock);
            //Thread.sleep(6000);
            System.out.println("===============updateAttribute==================");

            String attri = "attri";
            Map recordMap = new HashMap();
            recordMap.put("key0", "world0");
            //recordMap.put("key1", i);
            recordMap.put("keyNum", 1234589);
            recordMap.put("key2", false);


            System.out.println(JSON.toJSONString(recordMap));
            System.out.println(ontid);
            String hash = ontSdk.getOntIdTx().updateAttribute(ontid, "passwordtest", attri.getBytes(), "Json".getBytes(), JSON.toJSONString(recordMap).getBytes());
            System.out.println("hash:" + hash);

            //
            waitResult(ontSdk, lock);
            Thread.sleep(1000);


            System.out.println("===============getDDO==================");

            String ddo = ontSdk.getOntIdTx().getDDO(ontid);
            System.out.println("Ddo:" + ddo);

            //parse Attributes
            String rcd = JSON.parseObject(ddo).getJSONObject("Attributes").getString(attri);

            System.out.println("attri:" + attri);
            System.out.println("type:" + JSON.parseObject(rcd).get("Type"));
            System.out.println("value:" + JSON.parseObject(rcd).get("Value"));

            System.out.println();
            System.out.println("===============get Transaction, parse Attribute==================");
            InvokeCode t = (InvokeCode) ontSdk.getConnectMgr().getTransaction(hash);

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String waitUserid(OntSdk ontSdk, Object lock) {
        try {
            synchronized (lock) {
                while (true) {
                    lock.wait();
                    System.out.println(MsgQueue.getHeartBeat());
                    Result rt = JSON.parseObject(MsgQueue.getHeartBeat(), Result.class);
                    MsgQueue.setChangeFlag(false);
                    if (rt.Action.equals("heartbeat")) {
                        return (String) rt.Result;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void waitResult(OntSdk ontSdk, Object lock) {
        try {
            synchronized (lock) {
                System.out.println("\nwait begin " + new Date().toString());
                boolean flag = false;
                while (true) {
                    lock.wait();
//                    if(MsgQueue.getChangeFlag()){
//                        String wsSessionId = waitUserid(ontSdk,lock);
//                        ontSdk.getOntIdTx().setWsSessionId(wsSessionId);
//                    }
                    for (String e : MsgQueue.getResultSet()) {
                        System.out.println(e);
                        Result rt = JSON.parseObject(e, Result.class);
                        //TODO
                        MsgQueue.removeResult(e);
                        if (rt.Action.equals("Notify")) {
                            flag = true;
                            List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) rt.Result).get("State");
                            for (Map m : (List<Map<String, Object>>) (list.get(0).get("Value"))) {
                                String value = (String) m.get("Value");
                                String val = new String(Helper.hexToBytes(value));
                                System.out.print(val + " ");
                            }
                            System.out.println();
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
                System.out.println("wait end  " + new Date().toString() + "\n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static OntSdk getOntSdk() throws Exception {
//        String url = "http://54.222.182.88:22334";
        String url = "http://127.0.0.1:20384";
//        String url = "http://101.132.193.149:21334";
        OntSdk wm = OntSdk.getInstance();
        wm.setRestfulConnection(url);
        wm.openWalletFile("OntIdWsDemo.json");

        wm.setCodeAddress("263dbc0ca10aec184ceced7a998106733852c28a");
        return wm;
    }
}
