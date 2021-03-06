/**
 * Copyright 2013 Yamato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mod.ymt.air;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mod.ymt.air.cmn.Coord3D;

/**
 * @author Yamato
 *
 */
public class Serializer {
	private static final byte MAGIC_v1 = 0x4a;
	protected final AirCraftCore core = AirCraftCore.getInstance();
	
	public List<BlockData> deserialize(Coord3D basePoint, byte[] data) {
		List<BlockData> result = new ArrayList<BlockData>();
		try {
			DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
			try {
				byte format = input.readByte();
				switch (format) {
					case MAGIC_v1: {
						int size = input.readInt();
						try {
							for (int i = 0; i < size; i++) {
								BlockData b = BlockData.read(input, basePoint);
								if (b != null) {
									result.add(b);
								}
							}
						}
						catch (EOFException ex) {
							core.logFine("Serializer#deserialize unexpected eof occurred");
						}
						break;
					}
					default: {
						core.logFine("Serializer#deserialize unknown-format[%s]", format);
						break;
					}
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex) {
			core.logFine(ex, "Serializer#deserialize");
		}
		return result;
	}
	
	public byte[] serialize(Collection<BlockData> blocks) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DataOutputStream output = new DataOutputStream(buffer);
			try {
				output.writeByte(MAGIC_v1);
				output.writeInt(blocks.size());
				for (BlockData data: blocks) {
					data.write(output);
				}
				// ???????????????????????????????????? Packet250CustomPayload ????????????????????????????????????????????????
			}
			finally {
				output.close();
				buffer.close();
			}
			return buffer.toByteArray();
		}
		catch (IOException ex) {
			core.logFine(ex, "Serializer#serialize");
			return new byte[0];
		}
	}
}
