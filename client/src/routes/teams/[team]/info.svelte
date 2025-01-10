<script lang="ts">
	import type { Team } from '.';
	import { BadgeInfoIcon, CalendarIcon, EarthIcon, KeySquareIcon, ShieldIcon } from 'lucide-svelte';
	import { formatRelative } from 'date-fns';

	interface Props {
		team: Team;
	}

	let { team }: Props = $props();
</script>

<div>
	<div class="px-4 sm:px-0">
		<h3 class="text-base/7 font-semibold text-gray-900">Team information</h3>
		<p class="mt-1 max-w-2xl text-sm/6 text-gray-500">Details</p>
	</div>
	<div class="mt-6 border-t border-gray-100">
		<dl class="grid grid-cols-1 divide-y divide-gray-100 sm:grid-cols-2">
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<KeySquareIcon class="size-4" />
					<span>Name</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.name}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-2 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<BadgeInfoIcon class="size-4" />
					<span>Description</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.description}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-1 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<EarthIcon class="size-4" />
					<span>Country</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.country}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-1 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<ShieldIcon class="size-4" />
					<span>Captain</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">{team.captain}</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-1 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<CalendarIcon class="size-4" />
					<span>Created</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">
					{#if team.createdAt}
						{formatRelative(team.createdAt, Date.now())}
					{:else}
						Missing date
					{/if}
				</dd>
			</div>
			<div class="px-4 py-6 sm:col-span-1 sm:px-0">
				<dt class="flex items-center gap-1 text-sm/6 font-medium text-gray-900">
					<CalendarIcon class="size-4" />
					<span>Deleted</span>
				</dt>
				<dd class="mt-1 text-sm/6 text-gray-700 sm:mt-2">
					{#if team.deletedAt}
						{formatRelative(team.deletedAt, Date.now())}
					{:else}
						Not yet
					{/if}
				</dd>
			</div>
		</dl>
	</div>
</div>
