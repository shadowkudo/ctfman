<script lang="ts">
	import type { PageData } from './$types';
	import * as Card from '$lib/components/ui/card';
	import * as Tabs from '$lib/components/ui/tabs';
	import { Info, Challenges, Authors } from '.';

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
				<Tabs.Trigger value="challenges" class="grow text-center">Challenges</Tabs.Trigger>
				<Tabs.Trigger value="authors" class="grow text-center">Authors</Tabs.Trigger>
			</Tabs.List>
			<Tabs.Content value="info">
				<Info ctf={data.ctf} isOwner={data.ctf.owner == data.user?.name} />
			</Tabs.Content>
			<Tabs.Content value="challenges">
				<Challenges challenges={[]} />
			</Tabs.Content>
			<Tabs.Content value="authors">
				<Authors authors={[]} />
			</Tabs.Content>
		</Tabs.Root>
	</Card.Content>
</Card.Root>
