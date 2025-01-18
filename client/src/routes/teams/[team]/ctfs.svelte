<script lang="ts">
	import { fetchAllCtfs, type Ctf } from '$lib/data';
	import * as Table from '$lib/components/ui/table/index.js';
	import Button, { buttonVariants } from '$lib/components/ui/button/button.svelte';
	import { ChevronRightIcon, PlusIcon } from 'lucide-svelte';
	import * as Dialog from '$lib/components/ui/dialog/index.js';
	import { cn, useError } from '$lib/utils';
	import * as Select from '$lib/components/ui/select/index.js';
	import type { Team } from '.';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import { toast } from 'svelte-sonner';
	import { invalidateAll } from '$app/navigation';

	interface Props {
		ctfs: Ctf[];
		team: Team;
		isCaptain: boolean;
	}

	const { ctfs, team, isCaptain }: Props = $props();

	let allCtfs: Ctf[] = $state([]);
	let selectedCtf: string = $state('');
	let dialogOpen: boolean = $state(false);
	const availableCtfs: Ctf[] = $derived(
		allCtfs
			.filter((a) => a.status == 'ready' || a.status == 'in progress')
			.filter((a) => !ctfs.some((b) => a.title == b.title))
	);

	async function refreshAllCtfs() {
		allCtfs = await fetchAllCtfs(fetch);
	}

	async function joinCtf() {
		let res = await fetch(`${PUBLIC_BACKEND_URL}/teams/${team.name}/ctfs`, {
			method: 'POST',
			body: JSON.stringify({ ctf: selectedCtf }),
			credentials: 'include'
		});

		switch (res.status) {
			case 201:
				break;
			case 401:
				useError(401);
			case 409:
				toast.error('Error while joining', {
					description: 'The team already joined this ctf'
				});
				return;
			default:
				console.error(`join: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: `successfully joined ${selectedCtf}` });
		dialogOpen = false;
		invalidateAll();
	}
</script>

<Table.Root>
	<Table.Header>
		<Table.Row>
			<Table.Head class="w-64">Ctf</Table.Head>
			<Table.Head class="w-48">Status</Table.Head>
			<Table.Head>Description</Table.Head>
			<Table.Head class="w-64">Location</Table.Head>
			<Table.Head class="w-64">Owner</Table.Head>
			<Table.Head class="text-right">Actions</Table.Head>
		</Table.Row>
	</Table.Header>
	<Table.Body>
		{#each ctfs as ctf (ctf.title)}
			<Table.Row>
				<Table.Cell class="font-medium">{ctf.title}</Table.Cell>
				<Table.Cell>{ctf.status}</Table.Cell>
				<Table.Cell class="truncate">{ctf.description}</Table.Cell>
				<Table.Cell>{ctf.localisation}</Table.Cell>
				<Table.Cell>{ctf.owner}</Table.Cell>
				<Table.Cell class="flex flex-row justify-end gap-2">
					<Button href={`/ctfs/${ctf.title}`} variant="outline" size="icon">
						<ChevronRightIcon />
					</Button>
				</Table.Cell>
			</Table.Row>
		{/each}
	</Table.Body>
</Table.Root>
{#if isCaptain}
	<Dialog.Root bind:open={dialogOpen}>
		<Dialog.Trigger
			class={cn(buttonVariants({ variant: 'outline' }), 'mt-4 w-full')}
			onclick={refreshAllCtfs}
		>
			<PlusIcon />
			Join a CTF
		</Dialog.Trigger>
		<Dialog.Content>
			<Dialog.Header>
				<Dialog.Title>Join a ctf</Dialog.Title>
				<Dialog.Description>
					Only CTFs that are ready or in progress can be joined at this time
				</Dialog.Description>
			</Dialog.Header>
			<Select.Root type="single" bind:value={selectedCtf}>
				<Select.Trigger class="">{selectedCtf}</Select.Trigger>
				<Select.Content>
					{#each availableCtfs as ctf (ctf.title)}
						<Select.Item value={ctf.title}>{ctf.title}</Select.Item>
					{/each}
				</Select.Content>
			</Select.Root>
			<Dialog.Footer>
				<Button onclick={joinCtf}>Join</Button>
			</Dialog.Footer>
		</Dialog.Content>
	</Dialog.Root>
{/if}
