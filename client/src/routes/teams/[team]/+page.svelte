<script lang="ts">
	import type { PageData } from './$types';
	import * as Card from '$lib/components/ui/card';
	import * as Tabs from '$lib/components/ui/tabs';
	import { Info, Members, CTFs } from '.';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<Card.Root class="flex flex-col">
	<Card.Content class="grow">
		<Tabs.Root value="info" class="w-full">
			<Tabs.List class="flex w-full">
				<Tabs.Trigger value="info" class="grow text-center">Info</Tabs.Trigger>
				<Tabs.Trigger value="members" class="grow text-center">Members</Tabs.Trigger>
				<Tabs.Trigger value="ctfs" class="grow text-center">Ctfs</Tabs.Trigger>
			</Tabs.List>
			<Tabs.Content value="info">
				<Info team={data.team} />
			</Tabs.Content>
			<Tabs.Content value="members">
				<Members members={[]} />
			</Tabs.Content>
			<Tabs.Content value="ctfs">
				<CTFs />
			</Tabs.Content>
		</Tabs.Root>
	</Card.Content>
</Card.Root>
