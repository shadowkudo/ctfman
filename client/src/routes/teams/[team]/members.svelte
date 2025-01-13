<script lang="ts">
	import type { User } from '../..';

	interface Props {
		members: User[];
	}

	let { members }: Props = $props();
</script>

<p>TODO: list of members</p>
