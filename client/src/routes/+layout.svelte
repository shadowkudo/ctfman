<script lang="ts">
	import type { LayoutData } from './$types';
	import type { Snippet } from 'svelte';
	import { ModeWatcher } from 'mode-watcher';
	import { Toaster } from '$lib/components/ui/sonner';
	import '../app.css';
	import { page } from '$app/state';

	import { AppSidebar } from '$lib/components';
	import * as Sidebar from '$lib/components/ui/sidebar';
	import * as Breadcrumb from '$lib/components/ui/breadcrumb';
	import Separator from '$lib/components/ui/separator/separator.svelte';

	interface Props {
		data: LayoutData;
		children: Snippet<[]>;
	}

	let { children, data }: Props = $props();
</script>

<!-- mode watcher used to make sonner light mode until a dark theme is provided for the whole app -->
<ModeWatcher track={false} defaultMode="light" />
<!-- sonner for notifications -->
<Toaster />
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
						{#if page.route.id?.startsWith('/team')}
							<Breadcrumb.Item class="hidden md:block">
								<Breadcrumb.Link href="/teams">teams</Breadcrumb.Link>
							</Breadcrumb.Item>
							{#if page.route.id?.startsWith('/teams/[team]')}
								<Breadcrumb.Separator class="hidden md:block" />
								<Breadcrumb.Item>
									{#if page.route.id == '/teams/[team]'}
										<Breadcrumb.Page>{page.params?.team}</Breadcrumb.Page>
									{:else}
										<Breadcrumb.Link href={`/teams/${page.params?.team}`}>
											{page.params?.team}
										</Breadcrumb.Link>
									{/if}
								</Breadcrumb.Item>
							{/if}
						{/if}

						<!-- /ctfs/* -->
						{#if page.route.id?.startsWith('/ctf')}
							<Breadcrumb.Item class="hidden md:block">
								<Breadcrumb.Link href="/ctfs">ctfs</Breadcrumb.Link>
							</Breadcrumb.Item>
							{#if page.route.id?.startsWith('/ctfs/[ctf]')}
								<Breadcrumb.Separator class="hidden md:block" />
								<Breadcrumb.Item>
									<Breadcrumb.Page>[]</Breadcrumb.Page>
								</Breadcrumb.Item>
							{/if}
						{/if}

						<!-- **/edit|create -->
						{#if page.route.id?.endsWith('/edit')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>edit</Breadcrumb.Page>
							</Breadcrumb.Item>
						{:else if page.route.id?.endsWith('/create')}
							<Breadcrumb.Separator class="hidden md:block" />
							<Breadcrumb.Item>
								<Breadcrumb.Page>create</Breadcrumb.Page>
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
