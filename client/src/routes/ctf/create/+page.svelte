<script lang="ts">
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
	import { type DateValue } from '@internationalized/date';
	import { formatISO } from 'date-fns';

	import type { Status } from '$lib/data';

	interface CreateForm {
		title: string;
		description: string;
		localisation: string;
		status: Status;
		start?: DateValue;
		end?: DateValue;
	}

	interface Payload {
		title: string;
		description: string;
		localisation: string;
		status: Status;
		start: string | null;
		end: string | null;
	}

	let form: CreateForm = $state({
		title: '',
		description: '',
		localisation: '',
		status: 'wip'
	});

	async function submit(e: Event) {
		e.preventDefault();

		const payload: Payload = {
			...form,
			start: form.start ? formatISO(form.start.toString()) : null,
			end: form.end ? formatISO(form.end.toString()) : null
		};

		let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs`, {
			method: 'POST',
			body: JSON.stringify(payload),
			credentials: 'include'
		});

		switch (res.status) {
			case 201:
				break;
			case 401:
				useError(401);
			case 409:
				toast.error('Error while creating ctf', {
					description: 'A ctf already exists with this name'
				});
				return;
			default:
				console.error(`create: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: 'redirecting to the new ctf' });
		await goto(`/ctfs/${form.title}`, { invalidateAll: true });
	}

	const statusSelectContent = $derived(form.status.length ? form.status : 'Select a status');
</script>

<Card.Root>
	<Card.Header>
		<Card.Title>Create</Card.Title>
		<Card.Description>Create a ctf</Card.Description>
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
			<Button type="submit">Create</Button>
		</form>
	</Card.Content>
</Card.Root>
