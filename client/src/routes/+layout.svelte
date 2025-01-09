<script lang="ts">
	import type { LayoutData } from './$types';
	import type { Snippet } from 'svelte';
	import '../app.css';
	import { page } from '$app/state';

	import { AppSidebar } from '$lib/components';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import * as Breadcrumb from '$lib/components/ui/breadcrumb';
	import Separator from '$lib/components/ui/separator/separator.svelte';

	import { Base } from '$lib/components';
	import { cn } from '$lib/utils';
	import { BellIcon, HomeIcon, LandPlotIcon, MenuIcon, SearchIcon, UsersIcon } from 'lucide-svelte';

	interface Props {
		data: LayoutData;
		children: Snippet<[]>;
	}

	let { children, data }: Props = $props();
</script>

<Sidebar.Provider>
	<AppSidebar user={data.user} />
	<Sidebar.Inset>
		<header
			class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12"
		>
			<div class="flex items-center gap-2 px-4">
				<Sidebar.Trigger class="-ml-1" />
				<Separator orientation="vertical" class="mr-2 h-4" />
				<Breadcrumb.Root>
					<Breadcrumb.List>
						<!-- TODO: find a better way to handle the current breadcrumb -->
						{#if page.route.id == '/'}
							<Breadcrumb.Item class="hidden md:block">
								<Breadcrumb.Link href="/">home</Breadcrumb.Link>
							</Breadcrumb.Item>
						{/if}

						<!-- /teams/* -->
						{#if page.route.id?.startsWith('/teams')}
							<Breadcrumb.Item class="hidden md:block">
								<Breadcrumb.Link href="/teams">teams</Breadcrumb.Link>
							</Breadcrumb.Item>
						{/if}
						{#if page.route.id?.startsWith('/teams/create')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>create</Breadcrumb.Page>
							</Breadcrumb.Item>
						{/if}
						{#if page.route.id?.startsWith('/teams/t/[]')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>[]</Breadcrumb.Page>
							</Breadcrumb.Item>
						{/if}

						<!-- /ctfs/* -->
						{#if page.route.id?.startsWith('/ctfs')}
							<Breadcrumb.Item class="hidden md:block">
								<Breadcrumb.Link href="/ctfs">ctfs</Breadcrumb.Link>
							</Breadcrumb.Item>
						{/if}
						{#if page.route.id?.startsWith('/ctfs/create')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>create</Breadcrumb.Page>
							</Breadcrumb.Item>
						{/if}
						{#if page.route.id?.startsWith('/ctfs/c/[]')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>[]</Breadcrumb.Page>
							</Breadcrumb.Item>
						{/if}
					</Breadcrumb.List>
				</Breadcrumb.Root>
			</div>
		</header>
		<div class="flex flex-1 flex-col gap-4 p-4 pt-0">
			{@render children?.()}
		</div>
	</Sidebar.Inset>
</Sidebar.Provider>
