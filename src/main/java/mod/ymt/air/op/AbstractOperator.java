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

import java.util.HashSet;
import java.util.Set;
import mod.ymt.air.AirCraftCore;
import mod.ymt.air.BlockData;
import mod.ymt.air.Materializer;
import mod.ymt.air.Operator;
import mod.ymt.air.cmn.Coord3D;
import mod.ymt.air.cmn.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MaterialLogic;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author Yamato
 *
 */
public abstract class AbstractOperator implements Operator {
	private BlockRender render = null;
	
	@Override
	public int getPriority(BlockData data) {
		return PRIORITY_NORMAL;
	}
	
	@Override
	public BlockRender getRender() {
		if (render == null)
			render = newRender();
		return render;
	}
	
	@Override
	public void pickBlockFromWorld(Materializer owner, Coord3D pos, Coord3D base) {
		Block block = owner.world.getBlock(pos.x, pos.y, pos.z);
		int meta = owner.world.getBlockMetadata(pos.x, pos.y, pos.z);
		pickImitationBlock(owner, block, meta, pos, base);
	}
	
	@Override
	public void putBlocksToWorld(Materializer owner, BlockData data, Coord3D target, int rotate) {
		setRealBlock(owner, data, target, rotate);
	}
	
	@Override
	public void register(AirCraftCore core) {
		Set<Block> moveable = new HashSet<Block>();
		addMoveableBlocks(moveable);
		for (Block block: moveable) {
			core.setBlockOperator(block, this);
		}
	}
	
	protected abstract void addMoveableBlocks(Set<Block> result);
	
	protected boolean canPlaceBlockAt(World world, int x, int y, int z, Block block, int metadata) {
		return true;
	}
	
	protected void dropBlockAsItem(World world, Coord3D target, Block block, int metadata) {
		if (block != null) {
			if (Utils.isServerSide(world)) {
				block.dropBlockAsItem(world, target.x, target.y, target.z, metadata, 0);
			}
		}
	}
	
	protected boolean isNeedScheduleUpdate(Block block) {
		return false;
	}
	
	protected boolean isReplaceable(Block oldBlock, Block newBlock) {
		// ???????????????????????????????????????
		if (oldBlock == Blocks.bedrock) // ???????????? bedrock ?????????????????????
			return false;
		if (oldBlock == null || oldBlock.getMaterial().isReplaceable()) // ???????????? null ????????? Replaceable ?????????????????????
			return true;
		if (oldBlock.getMaterial() instanceof MaterialLogic) // ?????????????????????????????????????????????
			return true;
		return false; // ?????????????????????????????????
	}
	
	protected BlockRender newRender() {
		return new NormalRender();
	}
	
	protected void onCancelSetRealBlock(Materializer owner, BlockData data, Coord3D target) {
		// ??????????????????????????????????????????????????????
		dropBlockAsItem(owner.world, target, data.block, data.metadata);
	}
	
	protected void pickImitationBlock(Materializer owner, Block block, int metadata, Coord3D pos, Coord3D base) {
		if (block != null) {
			// ???????????????????????????????????????????????????
			NBTTagCompound tileData = readFromTileEntity(owner, block, metadata, pos);
			// ??????
			owner.setImitationBlock(new BlockData(block, metadata, pos.subtract(base), pos));
			if (tileData != null) {
				owner.setImitationTileEntity(tileData);
			}
		}
	}
	
	protected NBTTagCompound readFromTileEntity(Materializer owner, Block block, int metadata, Coord3D pos) {
		World world = owner.world;
		TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
		if (tile != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tile.writeToNBT(tag);
			return tag;
		}
		return null;
	}
	
	protected boolean setRealBlock(Materializer owner, BlockData data, Coord3D target, int rotate) {
		World world = owner.world;
		int x = target.x;
		int y = target.y;
		int z = target.z;
		Block oldBlock = world.getBlock(x, y, z);
		if (!isReplaceable(oldBlock, data.block)) { // ??????????????????
			// ???????????????
			onCancelSetRealBlock(owner, data, target);
			// ???????????????????????? false
			return false;
		}
		// ???????????????????????????????????????
		dropBlockAsItem(world, target, oldBlock, world.getBlockMetadata(x, y, z));
		// ??????????????????
		if (!setRealBlockWithRotation(owner, data.block, data.metadata, x, y, z, rotate)) { // ????????????
			// ???????????????
			onCancelSetRealBlock(owner, data, target);
			// ???????????????????????? false
			return false;
		}
		// ?????????????????????????????????
		writeToTileEntity(owner, data, target, rotate);
		
		// ???????????? true
		return true;
	}
	
	protected boolean setRealBlock(Materializer owner, Block block, int metadata, int x, int y, int z) {
		boolean result = false;
		if (block != null && canPlaceBlockAt(owner.world, x, y, z, block, metadata)) {
			// ??????
			result = owner.setRealBlock(x, y, z, block, metadata);
			// ????????????????????????
			if (result && isNeedScheduleUpdate(block)) {
				owner.world.scheduleBlockUpdate(x, y, z, block, block.tickRate(owner.world));
			}
		}
		return result;
	}
	
	protected abstract boolean setRealBlockWithRotation(Materializer owner, Block block, int metadata, int x, int y, int z, int rotate);
	
	protected void writeToTileEntity(Materializer owner, BlockData data, Coord3D target, int rotate) {
		if (data.block instanceof BlockContainer) {
			NBTTagCompound tag = owner.getImitationTileEntity(data.absPos);
			if (tag == null) {
				AirCraftCore.getInstance().logFine("writeToTileEntity not found TileEntity at ImitationSpace %s", target);
				return;
			}
			String id = tag.getString("id");
			// NBTTagCompound ??????
			TileEntity realTile = owner.world.getTileEntity(target.x, target.y, target.z);
			if (realTile == null) {
				AirCraftCore.getInstance().logFine("writeToTileEntity not found TileEntity at %s", target);
				return;
			}
			NBTTagCompound tag2 = new NBTTagCompound();
			realTile.writeToNBT(tag2);
			String id2 = tag2.getString("id");
			if (!id.equals(id2)) {
				AirCraftCore.getInstance().logFine("writeToTileEntity unmatch TileEntity at %s (%s - %s)", target, id, id2);
				return;
			}
			tag.setInteger("x", target.x);
			tag.setInteger("y", target.y);
			tag.setInteger("z", target.z);
			realTile.readFromNBT(tag); // ??????
		}
	}
	
	public static class NormalRender extends AbstractOperatorRender {
		@Override
		public boolean hasSpecialRender() {
			return false;
		}
		
		@Override
		public void renderBlock(RenderBlocks render, BlockData data) {
			Coord3D absPos = data.absPos;
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setNormal(0.0F, 1.0F, 0.0F);
			tessellator.setTranslation(-absPos.x - 0.5, -absPos.y, -absPos.z - 0.5);
			render.renderBlockByRenderType(data.block, absPos.x, absPos.y, absPos.z);
			tessellator.setTranslation(0, 0, 0);
			tessellator.draw();
		}
		
		@Override
		public void renderBlockSpecial(RenderManager manager, RenderBlocks render, BlockData data) {
			;
		}
	}
}
