import type { PlatformInfo, MenuActionHandlers } from '@lore/shared';

export interface MenuBuilderContext {
  platform: PlatformInfo;
  handlers: MenuActionHandlers;
}
