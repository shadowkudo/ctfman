<script lang="ts">
	import { goto } from '$app/navigation';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { Label } from '$lib/components/ui/label/index.js';
	import { toast } from 'svelte-sonner';

	let form = $state({
		name: '',
		password: '',
		email: '',
		role: 'CHALLENGER'
	});

	let registerResult: number | null = $state(null);

	async function register(e: Event) {
		e.preventDefault();

		let res = await fetch(`${PUBLIC_BACKEND_URL}/users`, {
			method: 'POST',
			body: JSON.stringify(form),
			credentials: 'include'
		});

		registerResult = res.status;

		if (res.status == 401) {
			return;
		}

		if (res.status == 409) {
			toast.error('Error', { description: 'Username or email already used' });
			return;
		}

		if (res.status != 201) {
			console.error(`register: unexpected response status: ${res.status}`);
			return;
		}

		toast.success('success', { description: 'please log in' });
		goto('/login', { invalidateAll: true });
	}
</script>

<div class="flex h-screen w-full items-center justify-center px-4">
	<Card.Root class="mx-auto max-w-sm">
		<Card.Header>
			<Card.Title class="text-2xl">Register</Card.Title>
			<Card.Description
				>Enter your username and email below to create to your account</Card.Description
			>
		</Card.Header>
		<Card.Content>
			<form
				class="grid gap-4"
				action={`${PUBLIC_BACKEND_URL}/users`}
				method="POST"
				onsubmit={register}
			>
				<div class="grid gap-2">
					<Label for="username">Username</Label>
					<Input id="username" type="text" placeholder="user1" bind:value={form.name} required />
				</div>
				<div class="grid gap-2">
					<Label for="email">Email</Label>
					<Input
						id="email"
						type="email"
						placeholder="user1@example.com"
						bind:value={form.email}
						required
					/>
				</div>
				<div class="grid gap-2">
					<div class="flex items-center">
						<Label for="password">Password</Label>
					</div>
					<Input id="password" type="password" bind:value={form.password} required />
				</div>
				<input type="hidden" bind:value={form.role} required />
				<Button type="submit" class="w-full">Register</Button>
			</form>
			<div class="mt-4 text-center text-sm">
				Already have an account?
				<a href="/login" class="underline"> Log in </a>
			</div>
		</Card.Content>
	</Card.Root>
</div>
