<script lang="ts">
	import type { PageData } from './$types';
	import * as Table from '$lib/components/ui/table/index.js';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import Button, { buttonVariants } from '$lib/components/ui/button/button.svelte';
	import { ChevronRightIcon, SettingsIcon, Trash2Icon } from 'lucide-svelte';
	import type { Team } from './[team]';
	import { useError } from '$lib/utils';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	async function deleteTeam(team: Team) {
		let res = await fetch(`${PUBLIC_BACKEND_URL}/teams/${team.name}`, {
			method: 'DELETE',
			credentials: 'include'
		});

		switch (res.status) {
			case 204:
				break;
			case 401:
			case 403:
				useError(res.status);
			default:
				console.error(`/teams/+page.svelte@deleteTeam: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: 'team deleted successfully' });
		goto(`/teams/`, { invalidateAll: true });
	}
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
				<Table.Cell>{team.country}</Table.Cell>
				<Table.Cell class="truncate">{team.description}</Table.Cell>
				<Table.Cell>{team.captain}</Table.Cell>
				<Table.Cell class="flex flex-row justify-end gap-2">
					{#if data.user?.name == team.captain}
						<Button href={`/teams/${team.name}/edit`} variant="outline" size="icon">
							<SettingsIcon />
						</Button>
					{/if}
					{#if data.user?.role.admin}
						<AlertDialog.Root>
							<AlertDialog.Trigger class={buttonVariants({ variant: 'outline', size: 'icon' })}>
								<Trash2Icon />
							</AlertDialog.Trigger>
							<AlertDialog.Content>
								<AlertDialog.Header>
									<AlertDialog.Title>Are you absolutely sure?</AlertDialog.Title>
									<AlertDialog.Description>
										This action cannot be undone. This will delete {team.name}.
									</AlertDialog.Description>
								</AlertDialog.Header>
								<AlertDialog.Footer>
									<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
									<AlertDialog.Action onclick={() => deleteTeam(team)}>Continue</AlertDialog.Action>
								</AlertDialog.Footer>
							</AlertDialog.Content>
						</AlertDialog.Root>
					{/if}
					<Button href={`/teams/${team.name}`} variant="outline" size="icon">
						<ChevronRightIcon />
					</Button>
				</Table.Cell>
			</Table.Row>
		{/each}
	</Table.Body>
</Table.Root>
