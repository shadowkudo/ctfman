import CTFs from './ctfs.svelte';
import Info from './info.svelte';
import Members from './members.svelte';

interface Team {
	name: string;
	description?: string;
	country?: string;
	captain?: string;
	createdAt?: Date;
	deletedAt?: Date;
}

export type { Team };
export { CTFs, Info, Members };
