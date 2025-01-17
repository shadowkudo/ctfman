<script lang="ts">
	import type { Ctf } from '..';
	import {
		BadgeInfoIcon,
		CalendarIcon,
		EarthIcon,
		KeySquareIcon,
		SettingsIcon,
		ShieldIcon,
		HeartPulseIcon
	} from 'lucide-svelte';
	import { formatRelative } from 'date-fns';
	import Button from '$lib/components/ui/button/button.svelte';

	interface Props {
		team: Ctf;
		isCaptain: boolean;
	}

	let { team, isCaptain }: Props = $props();
</script>

<div>
	<div class="flex gap-2 px-4 sm:px-0">
		<div class="grow">
			<h3 class="text-base/7 font-semibold text-gray-900">Team information</h3>
			<p class="mt-1 max-w-2xl text-sm/6 text-gray-500">Details</p>
		</div>
		{#if isCaptain}
			<Button href={`/ctfs/${team.title}/edit`} size="icon" variant="outline">
				<SettingsIcon />
			</Button>
		{/if}
	</div>
	<div class="mt-6 border-t border-gray-100">
		<dl class="grid grid-cols-1 divide-y divide-gray-100 sm:grid-cols-6">
			<div class="px-4 py-6 sm:col-span-6 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<KeySquareIcon class="size-4" />
					<span>Title</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.title}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-6 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<BadgeInfoIcon class="size-4" />
					<span>Description</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.description}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<HeartPulseIcon class="size-4" />
					<span>Satus</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.status}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<EarthIcon class="size-4" />
					<span>Location</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.localisation}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<ShieldIcon class="size-4" />
					<span>Owner</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.owner}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<CalendarIcon class="size-4" />
					<span>Created</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">
					{#if team.startedAt}
						{formatRelative(team.startedAt, Date.now())}
					{:else}
						TBD
					{/if}
				</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<CalendarIcon class="size-4" />
					<span>Deleted</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">
					{#if team.endedAt}
						{formatRelative(team.endedAt, Date.now())}
					{:else}
						TBD
					{/if}
				</dd>
			</div>
		</dl>
	</div>
</div>
