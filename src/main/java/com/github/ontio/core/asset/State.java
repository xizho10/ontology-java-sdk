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

package com.github.ontio.core.asset;

import com.github.ontio.common.Address;
import com.github.ontio.crypto.Digest;
import com.github.ontio.io.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class State implements Serializable {
    public byte version;
    public Address from;
    public Address to;
    public BigInteger value;

    public State(Address from, Address to, BigInteger amount){
        this.from = from;
        this.to = to;
        this.value = amount;
    }
    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        try {
            version = reader.readByte();
            from = reader.readSerializable(Address.class);
            to = reader.readSerializable(Address.class);
            value = new BigInteger(reader.readVarBytes());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.writeByte((byte)0);
        writer.writeSerializable(from);
        writer.writeSerializable(to);
        writer.writeVarBytes(value.toByteArray());

    }


    public Object json() {
        Map json = new HashMap<>();
        json.put("from", from.toHexString());
        json.put("to", to.toHexString());
        json.put("value", value.longValue());
        return json;
    }

}
