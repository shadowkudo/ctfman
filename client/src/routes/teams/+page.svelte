<script lang="ts">
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();
</script>

<div class="flex flex-row gap-2">
	{#each data.teams as team}
		<div>{team.name}</div>
	{/each}
</div>
