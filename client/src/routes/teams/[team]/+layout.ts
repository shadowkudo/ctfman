import type { LayoutLoad } from './$types';
import type { PageFetch } from '$lib/data';
import type { Team } from '.';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { useError } from '$lib/utils';

export const prerender = false;

export const load: LayoutLoad = async ({ fetch, params }) => {
	if (!params.team || !params.team.length) {
		useError(404);
	}

	let team: Team | null = await fetchTeam(fetch, params.team);

	if (!team) {
		useError(404);
	}

	return {
		team
	};
};

async function fetchTeam(fetch: PageFetch, name: string): Promise<Team | null> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/teams/${name}`, { credentials: 'include' });

	if (res.status != 200) {
		console.error(`teams/+page.tx@fetchUser: unexpected status: ${res.status}`);
		return null;
	}

	const json = await res.json();

	return {
		name: json.authentication,
		description: json.description,
		country: json.country,
		captain: json.captain,
		createdAt: json.createdAt ? new Date(json.createdAt) : undefined,
		deletedAt: json.deletedAt ? new Date(json.deletedAt) : undefined
	};
}
