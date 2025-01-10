<script lang="ts">
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	console.log(data);
</script>

<p>welcome to {data.team.name}</p>
