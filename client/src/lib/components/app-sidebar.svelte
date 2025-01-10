<script lang="ts">
	import NavMain from '$lib/components/nav-main.svelte';
	import NavTeams from '$lib/components/nav-teams.svelte';
	import NavUser from '$lib/components/nav-user.svelte';
	import * as Sidebar from '$lib/components/ui/sidebar/index.js';
	import type { ComponentProps } from 'svelte';
	import type { User } from '../../routes/+layout';
	import { LandPlotIcon } from 'lucide-svelte';
	import { HomeIcon, UsersIcon, FlagIcon } from 'lucide-svelte';

	import { page } from '$app/state';
	import { useSidebar } from '$lib/components/ui/sidebar/index.js';
	import { cn } from '$lib/utils';
	import { NavBuilder } from '$lib/nav';

	type Props = ComponentProps<typeof Sidebar.Root> & {
		user?: User;
	};

	let { ref = $bindable(null), collapsible = 'icon', user, ...restProps }: Props = $props();

	const sidebar = useSidebar();

	const navMain = $derived(
		new NavBuilder()
			.add('Home', '/', (home) => home.setIcon(HomeIcon))
			.addIf(user != null, 'Teams', '/teams', (team) => {
				team
					.setIcon(UsersIcon)
					.add('List', '/teams')
					.addIf(user?.role.challenger, 'Create', '/teams/create');
			})
			.addIf(user != null, 'CTFs', '/ctfs', (ctf) => {
				ctf
					.setIcon(FlagIcon)
					.add('List', '/ctfs')
					.addIf(user?.role.admin, 'Create', '/ctfs/create');
			})
			.get()
	);

	// TODO: remove teams here
	const teams = [
		{
			name: 'team1'
		},
		{
			name: 'team2'
		},
		{
			name: 'team3'
		}
	];
</script>

<Sidebar.Root bind:ref {collapsible} {...restProps}>
	<Sidebar.Header>
		<div
			class={cn(
				'flex h-16 shrink-0 items-center justify-center gap-4  text-indigo-600',
				sidebar.state == 'expanded' ? 'px-2' : ''
			)}
		>
			<LandPlotIcon></LandPlotIcon>
			{#if sidebar.state == 'expanded'}
				<p class="text-2xl font-semibold">CTFman</p>
			{/if}
		</div>
	</Sidebar.Header>
	<Sidebar.Content>
		<NavMain items={navMain} />
		<!-- TODO: replace with the user's teams -->
		{#if user}
			<NavTeams {teams} />
		{/if}
	</Sidebar.Content>
	<Sidebar.Footer>
		<NavUser {user} />
	</Sidebar.Footer>
	<Sidebar.Rail />
</Sidebar.Root>
