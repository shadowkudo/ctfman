<script lang="ts">
	import { page } from '$app/state';
	import { Button } from '$lib/components/ui/button';
</script>

<main class="grid grow place-items-center bg-white px-6 py-24 sm:py-32 lg:px-8">
	<div class="text-center">
		<p class="text-base font-semibold text-indigo-600">{page.status}</p>
		<h1 class="mt-4 text-balance text-5xl font-semibold tracking-tight text-gray-900 sm:text-7xl">
			{page.error?.message}
		</h1>
		{#if page.error?.subtext}
			<p class="mt-6 text-pretty text-lg font-medium text-gray-500 sm:text-xl/8">
				{page.error.subtext}
			</p>
		{/if}
		<div class="mt-10 flex items-center justify-center gap-x-6">
			{#if page.status == 401}
				<Button href="/login">Login</Button>
			{/if}
			<Button href="/">Go back home</Button>
		</div>
	</div>
</main>
