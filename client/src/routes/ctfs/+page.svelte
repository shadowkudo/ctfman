<script lang="ts">
	import type { PageData } from './$types';
	import * as Table from '$lib/components/ui/table/index.js';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import Button, { buttonVariants } from '$lib/components/ui/button/button.svelte';
	import { ChevronRightIcon, SettingsIcon, Trash2Icon } from 'lucide-svelte';
	import type { Ctf } from '.';
	import { useError } from '$lib/utils';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	async function deleteCtf(ctf: Ctf) {
		let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs/${ctf.title}`, {
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
				console.error(`/ctfs/+page.svelte@deleteCtf: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: 'ctf deleted successfully' });
		goto(`/ctfs/`, { invalidateAll: true });
	}
</script>

<Table.Root>
	<Table.Caption>A list of ctfs</Table.Caption>
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
		{#each data.ctfs as ctf (ctf.title)}
			<Table.Row>
				<Table.Cell class="font-medium">{ctf.title}</Table.Cell>
				<Table.Cell>{ctf.status}</Table.Cell>
				<Table.Cell class="truncate">{ctf.description}</Table.Cell>
				<Table.Cell>{ctf.localisation}</Table.Cell>
				<Table.Cell>{ctf.owner}</Table.Cell>
				<Table.Cell class="flex flex-row justify-end gap-2">
					{#if data.user?.name == ctf.owner}
						<Button href={`/ctfs/${ctf.title}/edit`} variant="outline" size="icon">
							<SettingsIcon />
						</Button>
						<AlertDialog.Root>
							<AlertDialog.Trigger class={buttonVariants({ variant: 'outline', size: 'icon' })}>
								<Trash2Icon />
							</AlertDialog.Trigger>
							<AlertDialog.Content>
								<AlertDialog.Header>
									<AlertDialog.Title>Are you absolutely sure?</AlertDialog.Title>
									<AlertDialog.Description>
										This action cannot be undone. This will delete {ctf.title}.
									</AlertDialog.Description>
								</AlertDialog.Header>
								<AlertDialog.Footer>
									<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
									<AlertDialog.Action onclick={() => deleteCtf(ctf)}>Continue</AlertDialog.Action>
								</AlertDialog.Footer>
							</AlertDialog.Content>
						</AlertDialog.Root>
					{/if}
					<Button href={`/ctfs/${ctf.title}`} variant="outline" size="icon">
						<ChevronRightIcon />
					</Button>
				</Table.Cell>
			</Table.Row>
		{/each}
	</Table.Body>
</Table.Root>
