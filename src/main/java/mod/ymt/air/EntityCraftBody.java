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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * @author Yamato
 *
 */
public class EntityCraftBody extends EntityFollower {
	public EntityCraftBody(World world) {
		this(world, null);
	}

	public EntityCraftBody(World world, String ownerName) {
		super(world, ownerName);
	}

	@Override
	public boolean interactFirst(EntityPlayer player) {
		if (core.tryInteractServer(worldObj)) {
			capturePassengers(); // 効果範囲にいる子を乗せる
			return true;
		}
		return false;
	}

	@Override
	public void setNextPosition(double x, double y, double z, float yaw, float pitch, int turn) {
		// x と z を 16 分の 1 にアラインメント
		x = Math.floor(x * 16) / 16;
		z = Math.floor(z * 16) / 16;
		// CraftBody では yaw と pitch を扱わない
		super.setNextPosition(x, y, z, 0, 0, turn);
	}
}
