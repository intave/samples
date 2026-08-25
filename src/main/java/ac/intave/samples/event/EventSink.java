/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package ac.intave.samples.event;


import java.io.Closeable;

public abstract class EventSink implements Closeable {
	public void visitSelect(Event event) {
		if (event instanceof AttackEvent) {
			visit((AttackEvent) event);
		} else if (event instanceof ClickEvent) {
			visit((ClickEvent) event);
		} else if (event instanceof ClientTickEndEvent) {
			visit((ClientTickEndEvent) event);
		} else if (event instanceof EntitySpawnEvent) {
			visit((EntitySpawnEvent) event);
		} else if (event instanceof EntityRemoveEvent) {
			visit((EntityRemoveEvent) event);
		} else if (event instanceof EntityMoveEvent) {
			visit((EntityMoveEvent) event);
		} else if (event instanceof PlayerInitEvent) {
			visit((PlayerInitEvent) event);
		} else if (event instanceof PlayerMoveEvent) {
			visit((PlayerMoveEvent) event);
		} else if (event instanceof SlotSwitchEvent) {
			visit((SlotSwitchEvent) event);
		} else if (event instanceof PropertiesEvent) {
			visit((PropertiesEvent) event);
		} else if (event instanceof BlockUpdatesEvent) {
			visit((BlockUpdatesEvent) event);
		} else if (event instanceof BlockPlaceEvent) {
			visit((BlockPlaceEvent) event);
		} else if (event instanceof BlockInteractEvent) {
			visit((BlockInteractEvent) event);
		} else if (event instanceof WindowClickEvent) {
			visit((WindowClickEvent) event);
		} else if (event instanceof WindowItemsEvent) {
			visit((WindowItemsEvent) event);
		} else if (event instanceof WindowActionEvent) {
			visit((WindowActionEvent) event);
		} else if (event instanceof InventoryOpenEvent) {
			visit((InventoryOpenEvent) event);
		} else if (event instanceof InventoryActionEvent) {
			visit((InventoryActionEvent) event);
		} else if (event instanceof InventoryUpdateEvent) {
			visit((InventoryUpdateEvent) event);
		} else if (event instanceof InventoryCloseEvent) {
			visit((InventoryCloseEvent) event);
		} else {
			visitAny(event);
		}
	}

	public void visit(PropertiesEvent event) {
		visitAny(event);
	}

	public void visit(BlockUpdatesEvent event) {
		visitAny(event);
	}

	public void visit(AttackEvent event) {
		visitAny(event);
	}

	public void visit(ClickEvent event) {
		visitAny(event);
	}

	public void visit(ClientTickEndEvent event) {
		visitAny(event);
	}

	public void visit(EntitySpawnEvent event) {
		visitAny(event);
	}

	public void visit(EntityRemoveEvent event) {
		visitAny(event);
	}

	public void visit(EntityMoveEvent event) {
		visitAny(event);
	}

	public void visit(PlayerInitEvent event) {
		visitAny(event);
	}

	public void visit(PlayerMoveEvent event) {
		visitAny(event);
	}

	public void visit(SlotSwitchEvent event) {
		visitAny(event);
	}

	public void visit(BlockPlaceEvent event) {
		visitAny(event);
	}

	public void visit(BlockInteractEvent event) {
		visitAny(event);
	}

	public void visit(WindowClickEvent event) {
		visitAny(event);
	}

	public void visit(WindowItemsEvent event) {
		visitAny(event);
	}

	public void visit(WindowActionEvent event) {
		visitAny(event);
	}

	public void visit(InventoryOpenEvent event) {
		visitAny(event);
	}

	public void visit(InventoryActionEvent event) {
		visitAny(event);
	}

	public void visit(InventoryUpdateEvent event) {
		visitAny(event);
	}

	public void visit(InventoryCloseEvent event) {
		visitAny(event);
	}

	public void visitAny(Event event) {
		// Your implementation here
	}

	public void close() {
		// Your implementation here
	}

	public abstract String name();
}
