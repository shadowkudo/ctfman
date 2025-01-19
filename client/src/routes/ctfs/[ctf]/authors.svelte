<script lang="ts">
	import type { User } from '../..';

	interface Props {
		authors: User[];
	}

	let { authors }: Props = $props();
</script>

<p>TODO: list of authors</p>
