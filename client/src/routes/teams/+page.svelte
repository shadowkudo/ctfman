<script lang="ts">
	import type { PageData } from './$types';
	import * as Table from '$lib/components/ui/table/index.js';
	import Button from '$lib/components/ui/button/button.svelte';
	import { ChevronRightIcon, SettingsIcon } from 'lucide-svelte';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<Table.Root>
	<Table.Caption>A list of teams</Table.Caption>
	<Table.Header>
		<Table.Row>
			<Table.Head class="w-64">Team</Table.Head>
			<Table.Head class="w-64">Country</Table.Head>
			<Table.Head>Description</Table.Head>
			<Table.Head class="w-64">Captain</Table.Head>
			<Table.Head class="text-right">Actions</Table.Head>
		</Table.Row>
	</Table.Header>
	<Table.Body>
		{#each data.teams as team (team.name)}
			<Table.Row>
				<Table.Cell class="font-medium">{team.name}</Table.Cell>
				<Table.Cell class="text-ellipsis">{team.country}</Table.Cell>
				<Table.Cell>{team.description}</Table.Cell>
				<Table.Cell>{team.captain}</Table.Cell>
				<Table.Cell class="flex flex-row justify-end gap-2">
					{#if data.user?.name == team.captain}
						<Button href={`/teams/${team.name}/edit`} variant="outline" size="icon">
							<SettingsIcon />
						</Button>
					{/if}
					<Button href={`/teams/${team.name}`} variant="outline" size="icon">
						<ChevronRightIcon />
					</Button>
				</Table.Cell>
			</Table.Row>
		{/each}
	</Table.Body>
</Table.Root>
