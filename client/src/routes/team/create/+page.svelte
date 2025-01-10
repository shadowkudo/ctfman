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

	interface CreateForm {
		name: string;
		password: string;
		description: string;
		country: string;
	}

	let form: CreateForm = $state({
		name: '',
		password: '',
		description: '',
		country: ''
	});

	async function submit(e: Event) {
		e.preventDefault();

		let res = await fetch(`${PUBLIC_BACKEND_URL}/teams`, {
			method: 'POST',
			body: JSON.stringify(form),
			credentials: 'include'
		});

		switch (res.status) {
			case 201:
				break;
			case 401:
				useError(401);
			case 409:
				toast.error('Error while creating team', {
					description: 'A team already exists with this name'
				});
				return;
			default:
				console.error(`login: unexpected response status: ${res.status}`);
				return;
		}

		toast.success('success', { description: 'redirecting to the new team' });
		await goto(`/teams/${form.name}`, { invalidateAll: true });
	}

	const countrySelectContent = $derived(
		form.country.length ? form.country : 'Select a country or leave empty'
	);
</script>

<Card.Root>
	<Card.Header>
		<Card.Title>Create</Card.Title>
		<Card.Description>Create a team</Card.Description>
	</Card.Header>
	<Card.Content>
		<form
			class="flex flex-col gap-4"
			action={`${PUBLIC_BACKEND_URL}/teams`}
			method="POST"
			onsubmit={submit}
		>
			<div class="mt-8 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
				<div class="sm:col-span-2">
					<Label for="name" class="block text-sm/6 font-medium text-gray-900">Team name</Label>
					<div class="mt-2">
						<Input
							bind:value={form.name}
							type="text"
							name="name"
							id="name"
							required
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="password" class="block text-sm/6 font-medium text-gray-900">Password</Label>
					<div class="mt-2">
						<Input
							bind:value={form.password}
							type="password"
							name="password"
							id="password"
							required
							autocomplete="new-password"
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>

				<div class="sm:col-span-2">
					<Label for="country" class="block text-sm/6 font-medium text-gray-900">Country</Label>
					<div class="mt-2 grid grid-cols-1">
						<Select.Root type="single" bind:value={form.country}>
							<Select.Trigger class="w-full" id="country">{countrySelectContent}</Select.Trigger>
							<Select.Content>
								<Select.Item value="Switzerland">Switzerland</Select.Item>
								<Select.Item value="USA">USA</Select.Item>
							</Select.Content>
						</Select.Root>
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
							class="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
						/>
					</div>
				</div>
			</div>
			<Button type="submit">Create</Button>
		</form>
	</Card.Content>
</Card.Root>
