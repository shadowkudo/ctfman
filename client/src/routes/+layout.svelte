<script lang="ts">
	import type { LayoutData } from './$types';
	import type { ComponentType, Snippet } from 'svelte';
	import '../app.css';
	import { page } from '$app/state';

	import { Base } from '$lib/components';
	import { cn } from '$lib/utils';
	import { BellIcon, HomeIcon, LandPlotIcon, MenuIcon, SearchIcon, UsersIcon } from 'lucide-svelte';

	interface Props {
		data: LayoutData;
		children: Snippet<[]>;
	}

	interface NavItem {
		name: string;
		href: string;
		current: boolean;
		Icon: ComponentType;
	}

	let { children, data }: Props = $props();

	const navigation: NavItem[] = $derived([
		{
			name: 'home',
			href: '/',
			current: page.route.id == '/',
			Icon: HomeIcon
		},
		{
			name: 'teams',
			href: '/teams',
			current: page.route.id?.startsWith('/teams') ?? false,
			Icon: UsersIcon
		}
	]);

	let sidebarOpen: boolean = $state(false);
</script>

<Base>
	<!-- TODO: mobile sidebar -->

	<!-- desktop sidebar -->
	<div class="hidden lg:relative lg:inset-y-0 lg:z-50 lg:flex lg:w-72 lg:flex-col">
		<!-- Sidebar component, swap this element with another sidebar if you like -->
		<div
			class="flex grow flex-col gap-y-5 overflow-y-auto border-r border-gray-200 bg-white px-6 pb-4"
		>
			<div class="flex h-16 shrink-0 items-center gap-4 text-indigo-600">
				<LandPlotIcon class=""></LandPlotIcon>
				<p class="text-xl font-semibold">CTFman</p>
			</div>
			<nav class="flex flex-1 flex-col">
				<ul role="list" class="flex flex-1 flex-col gap-y-7">
					<li>
						<ul role="list" class="-mx-2 space-y-1">
							{#if true}
								{#each navigation as item (item.name)}
									<li>
										<a
											href={item.href}
											class={cn(
												item.current
													? 'bg-gray-50 text-indigo-600'
													: 'text-gray-700 hover:bg-gray-50 hover:text-indigo-600',
												'group flex gap-x-3 rounded-md p-2 text-sm/6 font-semibold'
											)}
										>
											<item.Icon
												class={cn(
													item.current
														? 'text-indigo-600'
														: 'text-gray-400 group-hover:text-indigo-600',
													'size-6 shrink-0'
												)}
											></item.Icon>
											{item.name}
										</a>
									</li>
								{/each}
							{:else}
								<li>You must login</li>
							{/if}
						</ul>
					</li>
				</ul>
			</nav>
		</div>
	</div>

	<div class="grow">
		<div
			class="shadow-xs sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-gray-200 bg-white px-4 sm:gap-x-6 sm:px-6 lg:px-8"
		>
			<button
				type="button"
				class="-m-2.5 p-2.5 text-gray-700 lg:hidden"
				onclick={() => (sidebarOpen = true)}
			>
				<span class="sr-only">Open sidebar</span>
				<MenuIcon class="size-6" aria-hidden="true" />
			</button>

			<!-- Separator -->
			<div class="h-6 w-px bg-gray-200 lg:hidden" aria-hidden="true"></div>

			<div class="flex flex-1 gap-x-4 self-stretch lg:gap-x-6">
				<form class="grid flex-1 grid-cols-1" action="#" method="GET">
					<input
						type="search"
						name="search"
						aria-label="Search"
						class="outline-hidden col-start-1 row-start-1 block size-full bg-white pl-8 text-base text-gray-900 placeholder:text-gray-400 sm:text-sm/6"
						placeholder="Search"
					/>
					<SearchIcon
						class="pointer-events-none col-start-1 row-start-1 size-5 self-center text-gray-400"
						aria-hidden="true"
					/>
				</form>
				<div class="flex items-center gap-x-4 lg:gap-x-6">
					<button type="button" class="-m-2.5 p-2.5 text-gray-400 hover:text-gray-500">
						<span class="sr-only">View notifications</span>
						<BellIcon class="size-6" aria-hidden="true" />
					</button>

					<!-- Separator -->
					<div class="hidden lg:block lg:h-6 lg:w-px lg:bg-gray-200" aria-hidden="true"></div>

					<!-- Profile dropdown -->
					<!-- <Menu as="div" class="relative"> -->
					<!--   <MenuButton class="-m-1.5 flex items-center p-1.5"> -->
					<!--     <span class="sr-only">Open user menu</span> -->
					<!--     <img class="size-8 rounded-full bg-gray-50" src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80" alt="" /> -->
					<!--     <span class="hidden lg:flex lg:items-center"> -->
					<!--       <span class="ml-4 text-sm/6 font-semibold text-gray-900" aria-hidden="true">Tom Cook</span> -->
					<!--       <ChevronDownIcon class="ml-2 size-5 text-gray-400" aria-hidden="true" /> -->
					<!--     </span> -->
					<!--   </MenuButton> -->
					<!--   <transition enter-active-class="transition ease-out duration-100" enter-from-class="transform opacity-0 scale-95" enter-to-class="transform opacity-100 scale-100" leave-active-class="transition ease-in duration-75" leave-from-class="transform opacity-100 scale-100" leave-to-class="transform opacity-0 scale-95"> -->
					<!--     <MenuItems class="absolute right-0 z-10 mt-2.5 w-32 origin-top-right rounded-md bg-white py-2 ring-1 shadow-lg ring-gray-900/5 focus:outline-hidden"> -->
					<!--       <MenuItem v-for="item in userNavigation" :key="item.name" v-slot="{ active }"> -->
					<!--         <a :href="item.href" :class="[active ? 'bg-gray-50 outline-hidden' : '', 'block px-3 py-1 text-sm/6 text-gray-900']">{{ item.name }}</a> -->
					<!--       </MenuItem> -->
					<!--     </MenuItems> -->
					<!--   </transition> -->
					<!-- </Menu> -->
				</div>
			</div>
		</div>
		<main class="py-10">
			<div class="px-4 sm:px-6 lg:px-8">
				<!-- Your content -->
				{@render children?.()}
			</div>
		</main>
	</div>
</Base>
