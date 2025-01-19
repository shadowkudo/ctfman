<script lang="ts">
	import { invalidateAll } from '$app/navigation';
	import { PUBLIC_BACKEND_URL } from '$env/static/public';
	import { Button } from '$lib/components/ui/button/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import { Input } from '$lib/components/ui/input/index.js';
	import { Label } from '$lib/components/ui/label/index.js';
	import { redirect } from '@sveltejs/kit';

	let form = $state({
		username: '',
		password: ''
	});

	let loginResult: number | null = $state(null);

	async function logIn(e: Event) {
		e.preventDefault();

		let res = await fetch(`${PUBLIC_BACKEND_URL}/login`, {
			method: 'POST',
			body: JSON.stringify(form),
			credentials: 'include'
		});

		loginResult = res.status;

		if (res.status == 401) {
			return;
		}

		if (res.status != 204) {
			console.error(`login: unexpected response status: ${res.status}`);
			return;
		}

		await invalidateAll();
		redirect(307, '/');
	}
</script>

<Card.Root class="mx-auto max-w-sm">
	<Card.Header>
		<Card.Title class="text-2xl">Login</Card.Title>
		<Card.Description>Enter your username below to login to your account</Card.Description>
	</Card.Header>
	<Card.Content>
		<form class="grid gap-4" action={`${PUBLIC_BACKEND_URL}/login`} method="POST" onsubmit={logIn}>
			<div class="grid gap-2">
				<Label for="text">Username</Label>
				<Input id="text" type="text" placeholder="user1" bind:value={form.username} required />
			</div>
			<div class="grid gap-2">
				<div class="flex items-center">
					<Label for="password">Password</Label>
				</div>
				<Input id="password" type="password" bind:value={form.password} required />
			</div>
			<Button type="submit" class="w-full">Login</Button>
		</form>
		<div class="mt-4 text-center text-sm">
			Don't have an account?
			<a href="/register" class="underline"> Sign up </a>
		</div>
	</Card.Content>
</Card.Root>
