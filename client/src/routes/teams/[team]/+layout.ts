import type { LayoutLoad } from './$types';
import type { PageFetch } from '$lib/data';
import type { Team } from '.';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { useError } from '$lib/utils';
import type { Ctf } from '../../ctfs';

export const load: LayoutLoad = async ({ fetch, params }) => {
	if (!params.team || !params.team.length) {
		useError(404);
	}

	let team: Team | null = await fetchTeam(fetch, params.team);

	if (!team) {
		useError(404);
	}

	return {
		team,
		ctfs: await fetchCtfs(fetch, team.name)
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

async function fetchCtfs(fetch: PageFetch, teamName: string): Promise<Ctf[]> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/teams/${teamName}/ctfs`, { credentials: 'include' });

	if (res.status == 401) {
		useError(401);
	}

	if (res.status != 200) {
		console.error(`teams/[team]/+page.ts@fetchCtfs: unexpected status: ${res.status}`);
		return [];
	}

	const json = await res.json();

	if (!Array.isArray(json)) {
		console.error(`teams/[team]/+page.ts@fetchCtfs: unexpected json body: ${json}`);
		return [];
	}

	return json.map((it) => ({
		owner: it.owner,
		title: it.title,
		description: it.description,
		localisation: it.localisation,
		status: it.status,
		startedAt: it.startedAt ? new Date(it.startedAt) : null,
		endedAt: it.endedAt ? new Date(it.endedAt) : null
	}));
}
