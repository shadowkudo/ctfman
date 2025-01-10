<script lang="ts">
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	const { data }: Props = $props();
</script>

<p>edit page for {data.team.name}</p>
