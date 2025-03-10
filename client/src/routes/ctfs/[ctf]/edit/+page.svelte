<script lang="ts">
	import type { PageData } from './$types';
	import { goto } from '$app/navigation';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import Button from '$lib/components/ui/button/button.svelte';
	import * as Card from '$lib/components/ui/card/index.js';
	import Input from '$lib/components/ui/input/input.svelte';
	import Label from '$lib/components/ui/label/label.svelte';
	import * as Select from '$lib/components/ui/select';
	import Textarea from '$lib/components/ui/textarea/textarea.svelte';
	import { useError } from '$lib/utils';
	import { toast } from 'svelte-sonner';
	import { formatISO } from 'date-fns';
	import type { Status } from '$lib/data';

	interface Props {
		data: PageData;
	}

	interface UpdateForm {
		title: string;
		description: string;
		localisation: string;
		status: Status;
		start?: string;
		end?: string;
	}

	interface Payload {
		title?: string;
		description?: string;
		localisation?: string;
		status?: Status;
		start?: string | null;
		end?: string | null;
	}

	const { data }: Props = $props();

	let form: UpdateForm = $state({
		title: data.ctf.title,
		description: data.ctf.description,
		localisation: data.ctf.localisation,
		status: data.ctf.status,
		start: data.ctf.startedAt?.toISOString()?.slice(0, 16),
		end: data.ctf.endedAt?.toISOString()?.slice(0, 16)
	});

	async function submit(e: Event) {
		e.preventDefault();

		let payload: Payload = {
			title: form.title != data.ctf.title ? form.title : undefined,
			description: form.description != data.ctf.description ? form.description : undefined,
			localisation: form.localisation != data.ctf.localisation ? form.localisation : undefined,
			status: form.status != data.ctf.status ? form.status : undefined,
			start:
				form.start == data.ctf.startedAt?.toISOString()?.slice(0, 16)
					? undefined
					: form.start == ''
						? null
						: formatISO(form.start ?? ''),
			end:
				form.end == data.ctf.endedAt?.toISOString()?.slice(0, 16)
					? undefined
					: form.end == ''
						? null
						: formatISO(form.end ?? '')
		};

		let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs/${data.ctf.title}`, {
			method: 'PATCH',
			body: JSON.stringify(payload),
			credentials: 'include'
		});

		switch (res.status) {
			case 200:
				break;
			case 401:
			case 403:
				useError(res.status);
			case 409:
				toast.error('Error while updating ctf', {
					description: 'A ctf already exists with this name'
				});
				return;
			default:
				console.error(`/ctfs/[ctf]/edit: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: 'redirecting to the updated ctf' });
		goto(`/ctfs/${form.title}`);
	}

	const statusSelectContent = $derived(form.status.length ? form.status : 'Select a status');
</script>

<Card.Root>
	<Card.Header>
		<Card.Title>Edit</Card.Title>
		<Card.Description>Edit CTF: {data.ctf.title}</Card.Description>
	</Card.Header>
	<Card.Content>
		<form
			class="flex flex-col gap-4"
			action={`${PUBLIC_BACKEND_URL}/ctfs`}
			method="POST"
			onsubmit={submit}
		>
			<div class="mt-8 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
				<div class="sm:col-span-2">
					<Label for="title" class="block text-sm/6 font-medium text-gray-900">CTF title</Label>
					<div class="mt-2">
						<Input
							bind:value={form.title}
							type="text"
							name="title"
							id="title"
							required
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="location" class="block text-sm/6 font-medium text-gray-900">Location</Label>
					<div class="mt-2">
						<Input
							bind:value={form.localisation}
							type="text"
							name="location"
							id="location"
							required
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="status" class="block text-sm/6 font-medium text-gray-900">Status</Label>
					<div class="mt-2 grid grid-cols-1">
						<Select.Root type="single" bind:value={form.status} required>
							<Select.Trigger class="w-full" id="status">{statusSelectContent}</Select.Trigger>
							<Select.Content>
								<Select.Item value="wip">Wip</Select.Item>
								<Select.Item value="ready">Ready</Select.Item>
								<Select.Item value="in progress">In progress</Select.Item>
								<Select.Item value="finished">Finished</Select.Item>
							</Select.Content>
						</Select.Root>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="start" class="block text-sm/6 font-medium text-gray-900">Start</Label>
					<div class="mt-2">
						<Input
							bind:value={form.start}
							type="datetime-local"
							name="start"
							id="start"
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="end" class="block text-sm/6 font-medium text-gray-900">End</Label>
					<div class="mt-2">
						<Input
							bind:value={form.end}
							type="datetime-local"
							name="end"
							id="end"
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-6">
					<Label for="description" class="block text-sm/6 font-medium text-gray-900"
						>Description</Label
					>
					<div class="mt-2">
						<Textarea
							bind:value={form.description}
							id="description"
							name="description"
							required
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>
			</div>
			<Button type="submit">Save</Button>
		</form>
	</Card.Content>
</Card.Root>
