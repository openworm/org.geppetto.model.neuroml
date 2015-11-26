/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.model.neuroml.visitors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.neuroml.model.Cell;
import org.neuroml.model.Include;
import org.neuroml.model.Member;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.util.NeuroMLException;

public class CellUtils
{
	//Get all segments in the cell
	private LinkedHashMap<Integer, Segment> idsVsSegments;
	private Cell cell;
		
	public CellUtils(Cell cell){
		super();
		this.cell = cell;
		idsVsSegments = getIdsVsSegments();
	}
		
	public double calculateDistanceInGroup(double distance, Segment segment)
	{
		Point3DWithDiam proximal = (segment.getProximal() == null) ? idsVsSegments.get(segment.getParent().getSegment()).getDistal() : segment.getProximal();
		distance += distance(proximal, segment.getDistal());

		if(segment.getParent() != null)
		{
			return calculateDistanceInGroup(distance, idsVsSegments.get(segment.getParent().getSegment()));
		}
		return distance;
	}
	
    private LinkedHashMap<Integer, Segment> getIdsVsSegments() {

        LinkedHashMap<Integer, Segment> idsVsSegments = new LinkedHashMap<Integer, Segment>();
        for (Segment seg : cell.getMorphology().getSegment()) {
            idsVsSegments.put(seg.getId(), seg);
        }
        return idsVsSegments;
    }
	
    private double distance(Point3DWithDiam p, Point3DWithDiam d) {
        return Math.sqrt( Math.pow(p.getX()-d.getX(),2) + Math.pow(p.getY()-d.getY(),2) + Math.pow(p.getZ()-d.getZ(),2) );
    }
    
    public List<Segment> getSegmentsInGroup(Cell cell, String segmentGroup) throws NeuroMLException {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
                return getSegmentsInGroup(cell, namesVsSegmentGroups, sg);
            }
        }
        throw new NeuroMLException("No SegmentGroup: "+segmentGroup+" in cell with id: "+cell.getId());
    }
    
    public List<Segment> getSegmentsInGroup(Cell cell, LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) throws NeuroMLException {

        List<Segment> segsHere = new ArrayList<Segment>();
        
        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(idsVsSegments.get(memb.getSegment()));
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            List<Segment> segs = getSegmentsInGroup(cell, namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }
        
        return segsHere;
    }
    
    public LinkedHashMap<SegmentGroup, List<Integer>> getSegmentGroupsVsSegIds(Cell cell) {
        
        LinkedHashMap<SegmentGroup, List<Integer>> sgVsSegId = new LinkedHashMap<SegmentGroup, List<Integer>>();
        
        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            List<Integer> segsHere = getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            sgVsSegId.put(sg, segsHere);
        }
        
        return sgVsSegId;
    }
    
    public LinkedHashMap<String, SegmentGroup> getNamesVsSegmentGroups(Cell cell) {

        LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = new LinkedHashMap<String, SegmentGroup>();
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            namesVsSegmentGroups.put(sg.getId(), sg);
        }
        return namesVsSegmentGroups;
    }
    
    public List<Integer> getSegmentIdsInGroup(Cell cell, String segmentGroup) {
        
        for (SegmentGroup sg : cell.getMorphology().getSegmentGroup()) {
            if (sg.getId().equals(segmentGroup)) {
                LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups = getNamesVsSegmentGroups(cell);
                return getSegmentIdsInGroup(namesVsSegmentGroups, sg);
            }
        }
        return new ArrayList<Integer>();
    }
    
    public List<Integer> getSegmentIdsInGroup(LinkedHashMap<String, SegmentGroup> namesVsSegmentGroups, SegmentGroup segmentGroup) {

        List<Integer> segsHere = new ArrayList<Integer>();
        
        for (Member memb : segmentGroup.getMember()) {
            segsHere.add(memb.getSegment());
        }
        for (Include inc : segmentGroup.getInclude()) {
            String sg = inc.getSegmentGroup();
            List<Integer> segs = getSegmentIdsInGroup(namesVsSegmentGroups, namesVsSegmentGroups.get(sg));
            segsHere.addAll(segs);
        }
        
        return segsHere;
    }
}
