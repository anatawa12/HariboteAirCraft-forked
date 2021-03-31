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
package mod.ymt.air.op;

import java.util.Set;
import mod.ymt.air.BlockData;
import mod.ymt.air.Materializer;
import mod.ymt.air.cmn.Coord3D;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Yamato
 *
 */
public class NullOperator extends AbstractOperator {
	public static final NullOperator INSTANCE = new NullOperator();
	
	private NullOperator() {
		;
	}
	
	@Override
	protected void addMoveableBlocks(Set<Block> result) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected NBTTagCompound readFromTileEntity(Materializer owner, Block block, int metadata, Coord3D pos) {
		return null; // TileEntity は読み取らない
	}
	
	@Override
	protected boolean setRealBlockWithRotation(Materializer owner, Block block, int metadata, int x, int y, int z, int rotate) {
		return setRealBlock(owner, block, metadata, x, y, z);
	}
	
	@Override
	protected void writeToTileEntity(Materializer owner, BlockData data, Coord3D target, int rotate) {
		; // TileEntity に書き込まない
	}
}
