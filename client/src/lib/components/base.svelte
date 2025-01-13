<script lang="ts">
	import { cn } from '$lib/utils';

	let props = $props();
</script>

<div
	class={cn('relative isolate flex min-h-svh w-full bg-background max-lg:flex-col', props.class)}
>
	{@render props?.children?.()}
</div>
