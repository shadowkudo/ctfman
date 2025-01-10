<script lang="ts">
	import { page } from '$app/state';
</script>

<div class="flex grow items-center justify-center">
	<div class="flex flex-col items-center gap-4 text-4xl">
		<h2 class="text-7xl">{page.status}</h2>
		<h1 class="font-semibold">{page.error?.message}</h1>
	</div>
</div>
